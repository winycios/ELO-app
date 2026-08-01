package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.orcamento.OrcamentoRepository
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemProfissionalRS
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class StatusAgenda {
    /** O cliente pediu e o profissional ainda não respondeu. */
    Pendente,

    /** O profissional já enviou o orçamento final e espera o cliente. */
    AguardandoCliente,

    /** Cliente aprovou: é serviço marcado. */
    Confirmado,

    /** Serviço concluido. */
    Concluido;

    companion object {
        fun de(api: String?): StatusAgenda? = when (api?.trim()?.lowercase()) {
            "pendente" -> Pendente
            "orcamento_final" -> AguardandoCliente
            "aprovado" -> Confirmado
            "concluido" -> Concluido
            else -> null
        }
    }
}

data class ServicoAgendaUi(
    val id: Long,
    val dia: LocalDate,
    val inicioIso: String?,
    val fimIso: String?,
    val horarioPreferidoIso: String?,
    val nomeCliente: String,
    val fotoCliente: String?,
    val avaliacaoCliente: Double?,
    val descricao: String?,
    val valorTotal: Double?,
    val distanciaKm: Double?,
    val bairro: String?,
    val status: StatusAgenda,
) {
    val temProposta: Boolean get() = inicioIso != null
}

/** O dia vem da chave do mapa da API, que já agrupou o serviço na data certa. */
fun OrcamentoListagemProfissionalRS.paraAgenda(dia: LocalDate): ServicoAgendaUi? {
    val statusAgenda = StatusAgenda.de(status) ?: return null
    return ServicoAgendaUi(
        id = id,
        dia = dia,
        inicioIso = inicioProposto,
        fimIso = fimProposto,
        horarioPreferidoIso = horarioPreferido,
        nomeCliente = nomeUsuario.orEmpty(),
        fotoCliente = fotoUsuario,
        avaliacaoCliente = avaliacaoUsuario,
        descricao = descricao,
        valorTotal = valorTotal,
        distanciaKm = distanciaKm,
        bairro = bairro,
        status = statusAgenda,
    )
}

data class AgendaUi(
    val hoje: LocalDate,
    val diaSelecionado: LocalDate,
    val inicioSemana: LocalDate,
    /** Cada semana carregada fica em cache pela sua segunda-feira. */
    val semanas: Map<LocalDate, List<ServicoAgendaUi>> = emptyMap(),
    val carregando: Boolean = false,
    val erro: String? = null,
) {

    val dias: List<LocalDate> get() = (0L..6L).map { inicioSemana.plusDays(it) }

    /** `null` enquanto a semana nunca chegou da API — diferente de semana sem serviços. */
    private val servicosDaSemana: List<ServicoAgendaUi>? get() = semanas[inicioSemana]

    val semanaCarregada: Boolean get() = servicosDaSemana != null

    val servicosDoDia: List<ServicoAgendaUi>
        get() = servicosDe(diaSelecionado).sortedBy { horarioDeOrdenacao(it) }

    val totalPrevistoSemana: Double get() = servicosDaSemana.orEmpty().sumOf { it.valorTotal ?: 0.0 }

    val quantidadeSemana: Int get() = servicosDaSemana.orEmpty().size

    val aguardandoSemana: Int
        get() = servicosDaSemana.orEmpty().count {
            it.status == StatusAgenda.Pendente || it.status == StatusAgenda.AguardandoCliente
        }

    fun servicosDe(dia: LocalDate): List<ServicoAgendaUi> =
        servicosDaSemana.orEmpty().filter { it.dia == dia }

    private fun horarioDeOrdenacao(servico: ServicoAgendaUi): String =
        servico.inicioIso ?: servico.horarioPreferidoIso.orEmpty()
}

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = OrcamentoRepository(tokenStore)

    private val hoje = LocalDate.now()

    private val _estado = MutableStateFlow(
        AgendaUi(
            hoje = hoje,
            diaSelecionado = hoje,
            inicioSemana = inicioDaSemanaDe(hoje),
        ),
    )
    val estado: StateFlow<AgendaUi> = _estado.asStateFlow()

    private var busca: Job? = null

    fun abrirTela() = carregarSemana(_estado.value.inicioSemana)

    fun selecionarDia(dia: LocalDate) {
        val atual = _estado.value
        if (atual.diaSelecionado == dia) return
        val inicio = inicioDaSemanaDe(dia)
        _estado.update { it.copy(diaSelecionado = dia, inicioSemana = inicio) }
        carregarSemana(inicio)
    }

    fun semanaAnterior() = trocarSemana(-1)

    fun proximaSemana() = trocarSemana(1)

    fun irParaHoje() {
        val inicio = inicioDaSemanaDe(hoje)
        _estado.update { it.copy(diaSelecionado = hoje, inicioSemana = inicio) }
        carregarSemana(inicio)
    }

    /** Recarrega a semana visível ignorando o cache — usado no "tentar novamente" e ao voltar à tela. */
    fun recarregar() = carregarSemana(_estado.value.inicioSemana, forcar = true)

    private fun trocarSemana(semanas: Long) {
        val atual = _estado.value
        val novoInicio = atual.inicioSemana.plusWeeks(semanas)
        val deslocamento = atual.diaSelecionado.toEpochDay() - atual.inicioSemana.toEpochDay()
        _estado.update {
            it.copy(inicioSemana = novoInicio, diaSelecionado = novoInicio.plusDays(deslocamento))
        }
        carregarSemana(novoInicio)
    }

    private fun carregarSemana(inicio: LocalDate, forcar: Boolean = false) {
        busca?.cancel()
        if (!forcar && _estado.value.semanas.containsKey(inicio)) {
            _estado.update { it.copy(carregando = false, erro = null) }
            return
        }
        _estado.update { it.copy(carregando = true, erro = null) }
        busca = viewModelScope.launch {
            repository.listarAgenda(inicio.toString())
                .onSuccess { porDia ->
                    val servicos = porDia.flatMap { (data, itens) ->
                        val dia = runCatching { LocalDate.parse(data) }.getOrNull()
                            ?: return@flatMap emptyList()
                        itens.mapNotNull { it.paraAgenda(dia) }
                    }
                    _estado.update {
                        it.copy(
                            semanas = it.semanas + (inicio to servicos),
                            carregando = false,
                            erro = null,
                        )
                    }
                }
                .onFailure { erro ->
                    _estado.update {
                        it.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO)
                    }
                }
        }
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."

        fun inicioDaSemanaDe(dia: LocalDate): LocalDate =
            dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
}