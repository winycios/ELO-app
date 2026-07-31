package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.orcamento.OrcamentoRepository
import com.winyc.elo.backend.model.orcamento.OrcamentoCancelamentoRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheProfissionalRS
import com.winyc.elo.backend.model.orcamento.OrcamentoFinalCreateRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemProfissionalRS
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Abas da tela: sempre há uma selecionada, começando por "Novos". */
enum class FiltroOrcamentoPro(val api: String) {
    Novo("novo"),
    Enviado("enviado"),
    Aprovado("aprovado"),
    Historico("historico"),
}

data class OrcamentosProUi(
    val orcamentos: List<OrcamentoListagemProfissionalRS> = emptyList(),
    val filtro: FiltroOrcamentoPro = FiltroOrcamentoPro.Novo,
    val carregandoInicial: Boolean = false,
    val carregandoMais: Boolean = false,
    val hasNext: Boolean = true,
    val erro: String? = null,
) {

    val podeCarregarMais: Boolean
        get() = hasNext && !carregandoMais && !carregandoInicial
}

enum class VisaoOrcamentoPro { Detalhes, Orcar, OrcamentoFinal, Contato, Cancelar }

data class OrcamentoProDetalheUi(
    val orcamentoId: Long,
    val visao: VisaoOrcamentoPro,
    val carregando: Boolean = false,
    val detalhe: OrcamentoDetalheProfissionalRS? = null,
    val erro: String? = null,
    val salvando: Boolean = false,
    val erroAcao: String? = null,
)

class OrcamentosProViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = OrcamentoRepository(tokenStore)

    private val _estado = MutableStateFlow(OrcamentosProUi())
    val estado: StateFlow<OrcamentosProUi> = _estado.asStateFlow()

    private val _detalhe = MutableStateFlow<OrcamentoProDetalheUi?>(null)
    val detalhe: StateFlow<OrcamentoProDetalheUi?> = _detalhe.asStateFlow()

    private val _horarios = MutableStateFlow(HorariosUi())
    val horarios: StateFlow<HorariosUi> = _horarios.asStateFlow()

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    private var cursor: String? = null
    private var buscaLista: Job? = null
    private var servicoDoDetalhe: Long = -1L

    fun abrirTela() {
        if (_estado.value.orcamentos.isNotEmpty() || _estado.value.carregandoInicial) return
        carregarInicial()
    }

    fun carregarInicial() {
        buscaLista?.cancel()
        cursor = null
        _estado.update { it.copy(carregandoInicial = true, carregandoMais = false, erro = null) }
        buscaLista = viewModelScope.launch {
            repository.listarOrcamentosProfissional(
                status = _estado.value.filtro.api,
                cursor = null,
                tamanho = TAMANHO_PAGINA,
            )
                .onSuccess { pagina ->
                    _estado.update {
                        it.copy(
                            orcamentos = pagina.items,
                            carregandoInicial = false,
                            hasNext = pagina.hasNext,
                            erro = null,
                        )
                    }
                    cursor = pagina.nextCursor
                }
                .onFailure { erro ->
                    _estado.update {
                        it.copy(
                            carregandoInicial = false,
                            erro = erro.message ?: ERRO_GENERICO
                        )
                    }
                }
        }
    }

    fun carregarMais() {
        val atual = _estado.value
        if (!atual.podeCarregarMais || cursor == null) return
        _estado.update { it.copy(carregandoMais = true) }
        buscaLista = viewModelScope.launch {
            repository.listarOrcamentosProfissional(atual.filtro.api, cursor, TAMANHO_PAGINA)
                .onSuccess { pagina ->
                    _estado.update { estadoAtual ->
                        estadoAtual.copy(
                            orcamentos = estadoAtual.orcamentos + pagina.items,
                            carregandoMais = false,
                            hasNext = pagina.hasNext,
                        )
                    }
                    cursor = pagina.nextCursor
                }
                .onFailure { erro ->
                    _estado.update {
                        it.copy(
                            carregandoMais = false,
                            erro = erro.message ?: ERRO_GENERICO
                        )
                    }
                }
        }
    }

    /** Toda seleção de aba recarrega a lista na API, mesmo repetindo a aba atual. */
    fun selecionarFiltro(filtro: FiltroOrcamentoPro) {
        _estado.update {
            it.copy(filtro = filtro, orcamentos = emptyList(), hasNext = true, erro = null)
        }
        carregarInicial()
    }

    fun limparMensagem() {
        _mensagem.value = null
    }

    fun abrirDetalhe(orcamentoId: Long, visao: VisaoOrcamentoPro) {
        _detalhe.value =
            OrcamentoProDetalheUi(orcamentoId = orcamentoId, visao = visao, carregando = true)
        _horarios.value = HorariosUi()
        servicoDoDetalhe = -1L
        buscarDetalhe(orcamentoId, visao)
    }

    fun tentarNovamenteDetalhe() {
        val atual = _detalhe.value ?: return
        if (atual.carregando) return
        _detalhe.value = atual.copy(carregando = true, erro = null)
        buscarDetalhe(atual.orcamentoId, atual.visao)
    }

    fun fecharDetalhe() {
        _detalhe.value = null
    }

    fun limparErroAcao() {
        _detalhe.update { it?.copy(erroAcao = null) }
    }

    private fun buscarDetalhe(orcamentoId: Long, visao: VisaoOrcamentoPro) {
        viewModelScope.launch {
            repository.buscarOrcamentoPorIdProfissional(orcamentoId)
                .onSuccess { rs ->
                    _detalhe.update { atual ->
                        atual?.takeIf { it.orcamentoId == orcamentoId }
                            ?.copy(detalhe = rs, carregando = false, erro = null)
                            ?: atual
                    }
                    if (visao == VisaoOrcamentoPro.Orcar) {
                        servicoDoDetalhe = rs.solicitacao?.idServico ?: -1L
                        carregarSemana(dataReferencia = null)
                    }
                }
                .onFailure { erro ->
                    _detalhe.update { atual ->
                        atual?.takeIf { it.orcamentoId == orcamentoId }
                            ?.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO)
                            ?: atual
                    }
                }
        }
    }

    fun tentarNovamenteHorarios() = carregarSemana(dataReferencia = null)

    fun proximaSemana() {
        val inicio = inicioSemanaAtual()?.plusWeeks(1) ?: return
        carregarSemana(inicio.toString())
    }

    fun semanaAnterior() {
        if (!_horarios.value.podeVoltarSemana) return
        val inicio = inicioSemanaAtual()?.minusWeeks(1) ?: return
        carregarSemana(inicio.toString())
    }

    private fun carregarSemana(dataReferencia: String?) {
        if (servicoDoDetalhe <= 0 || _horarios.value.carregando) return
        _horarios.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            repository.buscarHorariosDisponiveis(servicoDoDetalhe, dataReferencia)
                .onSuccess { rs ->
                    _horarios.update {
                        it.copy(
                            semana = rs,
                            carregando = false,
                            erro = null,
                            podeVoltarSemana = semanaNoFuturo(rs.inicioSemana),
                        )
                    }
                }
                .onFailure { erro ->
                    _horarios.update {
                        it.copy(
                            carregando = false,
                            erro = erro.message ?: ERRO_GENERICO
                        )
                    }
                }
        }
    }

    private fun inicioSemanaAtual(): LocalDate? =
        _horarios.value.semana?.inicioSemana?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    private fun semanaNoFuturo(inicioSemana: String?): Boolean {
        val inicio =
            inicioSemana?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return false
        return inicio.isAfter(LocalDate.now())
    }

    // ----- Ações do profissional -----

    fun enviarOrcamentoFinal(
        inicioProposto: String,
        fimProposto: String,
        observacao: String,
        custos: List<OrcamentoFinalCreateRQ.CustoRQ>,
    ) {
        val atual = _detalhe.value ?: return
        if (atual.salvando || custos.isEmpty()) return
        executarAcao(atual.orcamentoId, MSG_ENVIADO) {
            repository.enviarOrcamentoFinal(
                atual.orcamentoId,
                OrcamentoFinalCreateRQ(
                    inicioProposto = inicioProposto,
                    fimProposto = fimProposto,
                    observacaoProfissional = observacao.trim().takeIf { it.isNotBlank() },
                    custos = custos,
                ),
            )
        }
    }

    fun cancelarOrcamento(motivo: String, descricao: String) {
        val atual = _detalhe.value ?: return
        if (atual.salvando) return
        val pendente = atual.detalhe?.status?.trim().equals("pendente", ignoreCase = true)
        executarAcao(atual.orcamentoId, if (pendente) MSG_RECUSADO else MSG_CANCELADO) {
            repository.cancelarOrcamentoProfissional(
                atual.orcamentoId,
                OrcamentoCancelamentoRQ(motivo = motivo.trim(), descricao = descricao.trim()),
            )
        }
    }

    private fun executarAcao(
        orcamentoId: Long,
        mensagemSucesso: String,
        acao: suspend () -> Result<OrcamentoDetalheProfissionalRS>,
    ) {
        _detalhe.update { it?.copy(salvando = true, erroAcao = null) }
        viewModelScope.launch {
            acao()
                .onSuccess {
                    _detalhe.value = null
                    _mensagem.value = mensagemSucesso
                    carregarInicial()
                }
                .onFailure { erro ->
                    _detalhe.update { atual ->
                        atual?.takeIf { it.orcamentoId == orcamentoId }
                            ?.copy(salvando = false, erroAcao = erro.message ?: ERRO_GENERICO)
                            ?: atual
                    }
                }
        }
    }

    private companion object {
        const val TAMANHO_PAGINA = 20
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
        const val MSG_ENVIADO = "Orçamento enviado ao cliente!"
        const val MSG_RECUSADO = "Solicitação recusada."
        const val MSG_CANCELADO = "Serviço cancelado."
    }
}
