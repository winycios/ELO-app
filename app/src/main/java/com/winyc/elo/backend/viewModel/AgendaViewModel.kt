package com.winyc.elo.backend.viewModel

import androidx.lifecycle.ViewModel
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemProfissionalRS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
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

fun OrcamentoListagemProfissionalRS.paraAgenda(): ServicoAgendaUi? {
    val statusAgenda = StatusAgenda.de(status) ?: return null
    val referencia = inicioProposto ?: horarioPreferido ?: return null
    val dia = runCatching { LocalDateTime.parse(referencia).toLocalDate() }.getOrNull() ?: return null
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
    val servicos: List<ServicoAgendaUi> = emptyList(),
) {

    val dias: List<LocalDate> get() = (0L..6L).map { inicioSemana.plusDays(it) }

    val servicosDoDia: List<ServicoAgendaUi>
        get() = servicos.filter { it.dia == diaSelecionado }.sortedBy { horarioDeOrdenacao(it) }

    private val servicosDaSemana: List<ServicoAgendaUi>
        get() = servicos.filter { it.dia >= inicioSemana && it.dia <= inicioSemana.plusDays(6) }

    val totalPrevistoSemana: Double get() = servicosDaSemana.sumOf { it.valorTotal ?: 0.0 }

    val quantidadeSemana: Int get() = servicosDaSemana.size

    val aguardandoSemana: Int
        get() = servicosDaSemana.count {
            it.status == StatusAgenda.Pendente || it.status == StatusAgenda.AguardandoCliente
        }

    fun servicosDe(dia: LocalDate): List<ServicoAgendaUi> = servicos.filter { it.dia == dia }

    private fun horarioDeOrdenacao(servico: ServicoAgendaUi): String =
        servico.inicioIso ?: servico.horarioPreferidoIso.orEmpty()
}

class AgendaViewModel : ViewModel() {

    private val hoje = LocalDate.now()

    private val _estado = MutableStateFlow(
        AgendaUi(
            hoje = hoje,
            diaSelecionado = hoje,
            inicioSemana = inicioDaSemanaDe(hoje),
        ),
    )
    val estado: StateFlow<AgendaUi> = _estado.asStateFlow()

    init {
        carregarAgenda()
    }

    fun selecionarDia(dia: LocalDate) {
        if (_estado.value.diaSelecionado == dia) return
        _estado.update { it.copy(diaSelecionado = dia, inicioSemana = inicioDaSemanaDe(dia)) }
    }

    fun semanaAnterior() = trocarSemana(-1)

    fun proximaSemana() = trocarSemana(1)

    fun irParaHoje() {
        _estado.update { it.copy(diaSelecionado = hoje, inicioSemana = inicioDaSemanaDe(hoje)) }
    }

    private fun trocarSemana(semanas: Long) {
        _estado.update { atual ->
            val novoInicio = atual.inicioSemana.plusWeeks(semanas)
            val deslocamento = atual.diaSelecionado.toEpochDay() - atual.inicioSemana.toEpochDay()
            atual.copy(
                inicioSemana = novoInicio,
                diaSelecionado = novoInicio.plusDays(deslocamento),
            )
        }
    }

    private fun carregarAgenda() {
        val servicos = agendaMockada(hoje).mapNotNull { it.paraAgenda() }
        _estado.update { it.copy(servicos = servicos) }
    }

    private companion object {
        fun inicioDaSemanaDe(dia: LocalDate): LocalDate =
            dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
}

/* ---------------------------- Dados mockados ---------------------------- */

private fun agendaMockada(hoje: LocalDate): List<OrcamentoListagemProfissionalRS> {
    fun quando(dia: LocalDate, hora: String) = "${dia}T$hora"

    val ontem = hoje.minusDays(1)
    val amanha = hoje.plusDays(1)
    val depois = hoje.plusDays(2)

    return listOf(
        OrcamentoListagemProfissionalRS(
            id = 1,
            idServico = 11,
            nomeUsuario = "Marina Alves",
            avaliacaoUsuario = 4.8,
            categoria = "Elétrica",
            descricao = "Instalação elétrica",
            distanciaKm = 3.2,
            bairro = "Vila Mariana",
            dataHoraCriacao = quando(ontem, "08:10"),
            horarioPreferido = quando(hoje, "09:00"),
            inicioProposto = quando(hoje, "09:00"),
            fimProposto = quando(hoje, "11:00"),
            valorTotal = 320.0,
            status = "aprovado",
        ),
        OrcamentoListagemProfissionalRS(
            id = 2,
            idServico = 12,
            nomeUsuario = "Rafael Lima",
            avaliacaoUsuario = 5.0,
            categoria = "Hidráulica",
            descricao = "Manutenção hidráulica",
            distanciaKm = 7.8,
            bairro = "Moema",
            dataHoraCriacao = quando(ontem, "12:40"),
            horarioPreferido = quando(hoje, "14:00"),
            inicioProposto = quando(hoje, "14:00"),
            fimProposto = quando(hoje, "15:30"),
            valorTotal = 180.0,
            status = "aprovado",
        ),
        OrcamentoListagemProfissionalRS(
            id = 3,
            idServico = 13,
            nomeUsuario = "Julia Costa",
            avaliacaoUsuario = 4.9,
            categoria = "Elétrica",
            descricao = "Reparo de tomadas",
            distanciaKm = 2.4,
            bairro = "Pinheiros",
            dataHoraCriacao = quando(hoje, "07:05"),
            horarioPreferido = quando(hoje, "17:00"),
            status = "pendente",
        ),
        OrcamentoListagemProfissionalRS(
            id = 4,
            idServico = 14,
            nomeUsuario = "Bruno Tavares",
            avaliacaoUsuario = 4.9,
            categoria = "Elétrica",
            descricao = "Revisão de fiação",
            distanciaKm = 4.3,
            bairro = "Perdizes",
            dataHoraCriacao = quando(ontem.minusDays(2), "09:00"),
            horarioPreferido = quando(ontem, "10:00"),
            inicioProposto = quando(ontem, "10:00"),
            fimProposto = quando(ontem, "12:30"),
            valorTotal = 410.0,
            status = "concluido",
        ),
        OrcamentoListagemProfissionalRS(
            id = 5,
            idServico = 15,
            nomeUsuario = "Carla Souza",
            avaliacaoUsuario = 4.7,
            categoria = "Elétrica",
            descricao = "Troca de disjuntores",
            distanciaKm = 5.1,
            bairro = "Tatuapé",
            dataHoraCriacao = quando(hoje, "10:20"),
            horarioPreferido = quando(amanha, "08:00"),
            inicioProposto = quando(amanha, "08:00"),
            fimProposto = quando(amanha, "09:30"),
            valorTotal = 260.0,
            status = "orcamento_final",
        ),
        OrcamentoListagemProfissionalRS(
            id = 6,
            idServico = 16,
            nomeUsuario = "Diego Martins",
            avaliacaoUsuario = 4.6,
            categoria = "Hidráulica",
            descricao = "Troca de registro",
            distanciaKm = 6.0,
            bairro = "Santana",
            dataHoraCriacao = quando(hoje, "11:00"),
            horarioPreferido = quando(depois, "13:00"),
            inicioProposto = quando(depois, "13:00"),
            fimProposto = quando(depois, "14:00"),
            valorTotal = 390.0,
            status = "aprovado",
        ),
        OrcamentoListagemProfissionalRS(
            id = 7,
            idServico = 17,
            nomeUsuario = "Helena Prado",
            avaliacaoUsuario = 5.0,
            categoria = "Elétrica",
            descricao = "Instalação de chuveiro",
            distanciaKm = 1.9,
            bairro = "Itaim Bibi",
            dataHoraCriacao = quando(hoje, "11:30"),
            horarioPreferido = quando(depois, "16:00"),
            status = "pendente",
        ),
    )
}
