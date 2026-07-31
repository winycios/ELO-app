package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.orcamento.OrcamentoRepository
import com.winyc.elo.backend.model.orcamento.OrcamentoCancelamentoRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheRS
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemRS
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeusOrcamentosUi(
    val orcamentos: List<OrcamentoListagemRS> = emptyList(),
    val statusSelecionado: String? = null,
    val carregandoInicial: Boolean = false,
    val carregandoMais: Boolean = false,
    val hasNext: Boolean = true,
    val erro: String? = null,
) {

    val podeCarregarMais: Boolean
        get() = hasNext && !carregandoMais && !carregandoInicial
}

enum class VisaoOrcamento { Detalhes, OrcamentoFinal, Contato, Cancelar }

data class OrcamentoDetalheUi(
    val orcamentoId: Long,
    val visao: VisaoOrcamento,
    val carregando: Boolean = false,
    val detalhe: OrcamentoDetalheRS? = null,
    val erro: String? = null,
    val salvando: Boolean = false,
    val erroAcao: String? = null,
)

class MeusOrcamentosViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = OrcamentoRepository(tokenStore)

    private val _estado = MutableStateFlow(MeusOrcamentosUi())
    val estado: StateFlow<MeusOrcamentosUi> = _estado.asStateFlow()

    private val _detalhe = MutableStateFlow<OrcamentoDetalheUi?>(null)
    val detalhe: StateFlow<OrcamentoDetalheUi?> = _detalhe.asStateFlow()

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    private var cursor: String? = null
    private var buscaLista: Job? = null

    fun abrirTela(logado: Boolean) {
        buscaLista?.cancel()
        _detalhe.value = null
        cursor = null
        _estado.value = MeusOrcamentosUi()
        if (logado) carregarInicial()
    }

    fun carregarInicial() {
        buscaLista?.cancel()
        cursor = null
        _estado.update { it.copy(carregandoInicial = true, carregandoMais = false, erro = null) }
        buscaLista = viewModelScope.launch {
            repository.listarOrcamentos(_estado.value.statusSelecionado, cursor = null, tamanho = TAMANHO_PAGINA)
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
                    _estado.update { it.copy(carregandoInicial = false, erro = erro.message ?: ERRO_GENERICO) }
                }
        }
    }

    fun carregarMais() {
        val atual = _estado.value
        if (!atual.podeCarregarMais || cursor == null) return
        _estado.update { it.copy(carregandoMais = true) }
        buscaLista = viewModelScope.launch {
            repository.listarOrcamentos(atual.statusSelecionado, cursor, TAMANHO_PAGINA)
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
                    _estado.update { it.copy(carregandoMais = false, erro = erro.message ?: ERRO_GENERICO) }
                }
        }
    }

    fun selecionarStatus(status: String?) {
        if (_estado.value.statusSelecionado == status) return
        _estado.update {
            it.copy(statusSelecionado = status, orcamentos = emptyList(), hasNext = true, erro = null)
        }
        carregarInicial()
    }

    // ----- Detalhe (detalhes, orçamento final e contato) -----

    fun abrirDetalhe(orcamentoId: Long, visao: VisaoOrcamento) {
        _detalhe.value = OrcamentoDetalheUi(orcamentoId = orcamentoId, visao = visao, carregando = true)
        buscarDetalhe(orcamentoId)
    }

    fun tentarNovamenteDetalhe() {
        val atual = _detalhe.value ?: return
        if (atual.carregando) return
        _detalhe.value = atual.copy(carregando = true, erro = null)
        buscarDetalhe(atual.orcamentoId)
    }

    fun fecharDetalhe() {
        _detalhe.value = null
    }

    fun limparMensagem() {
        _mensagem.value = null
    }

    fun trocarVisao(visao: VisaoOrcamento) {
        _detalhe.update { it?.copy(visao = visao, erroAcao = null) }
    }

    fun aprovarOrcamentoFinal() {
        val atual = _detalhe.value ?: return
        if (atual.salvando) return
        executarAcao(atual.orcamentoId, MSG_APROVADO) {
            repository.aprovarOrcamentoFinal(atual.orcamentoId)
        }
    }

    /** Cancela a solicitação (pendente, com orçamento final ou já aprovada). */
    fun cancelarOrcamento(motivo: String, descricao: String) {
        val atual = _detalhe.value ?: return
        if (atual.salvando) return
        executarAcao(atual.orcamentoId, MSG_CANCELADO) {
            repository.cancelarOrcamentoCliente(
                atual.orcamentoId,
                OrcamentoCancelamentoRQ(motivo = motivo.trim(), descricao = descricao.trim()),
            )
        }
    }

    /** Executa a ação, fecha a folha no sucesso e recarrega a lista (o status muda). */
    private fun executarAcao(
        orcamentoId: Long,
        mensagemSucesso: String,
        acao: suspend () -> Result<OrcamentoDetalheRS>,
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

    private fun buscarDetalhe(orcamentoId: Long) {
        viewModelScope.launch {
            repository.buscarOrcamentoPorId(orcamentoId)
                .onSuccess { rs ->
                    _detalhe.update { atual ->
                        atual?.takeIf { it.orcamentoId == orcamentoId }
                            ?.copy(detalhe = rs, carregando = false, erro = null)
                            ?: atual
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

    private companion object {
        const val TAMANHO_PAGINA = 20
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
        const val MSG_APROVADO = "Orçamento aceito! O profissional foi avisado."
        const val MSG_CANCELADO = "Orçamento cancelado."
    }
}
