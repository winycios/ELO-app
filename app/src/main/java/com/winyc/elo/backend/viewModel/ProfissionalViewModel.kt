package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.profissional.ProfissionalRepository
import com.winyc.elo.backend.model.servico.ServicoCreateDTO
import com.winyc.elo.backend.model.servico.ServicoListaRS
import com.winyc.elo.backend.model.servico.ServicoRS
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfissionalUi(
    val servicos: List<ServicoListaRS> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
)

class ProfissionalViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = ProfissionalRepository(tokenStore)

    private val _estado = MutableStateFlow(ProfissionalUi())
    val estado: StateFlow<ProfissionalUi> = _estado.asStateFlow()

    init {
        if (tokenStore.estaLogado) carregar()
        observarSessao()
    }

    private fun observarSessao() {
        viewModelScope.launch {
            tokenStore.estaLogadoFlow.drop(1).collect { logado ->
                _estado.value = ProfissionalUi()
                if (logado) carregar()
            }
        }
    }

    fun carregar() {
        if (!tokenStore.estaLogado || _estado.value.carregando) return
        _estado.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            repository.listarServicos()
                .onSuccess { lista ->
                    _estado.update { it.copy(servicos = lista, carregando = false, erro = null) }
                }
                .onFailure { erro ->
                    _estado.update { it.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO) }
                }
        }
    }

    /** Cria ou edita (quando o DTO traz `id`) e recarrega a lista ao concluir. */
    fun salvarServico(dto: ServicoCreateDTO, onResultado: (Boolean) -> Unit = {}) {
        if (_estado.value.salvando) return
        _estado.update { it.copy(salvando = true, erro = null) }
        viewModelScope.launch {
            repository.salvarServico(dto)
                .onSuccess {
                    _estado.update { it.copy(salvando = false) }
                    carregar()
                    onResultado(true)
                }
                .onFailure { erro ->
                    _estado.update { it.copy(salvando = false, erro = erro.message ?: ERRO_GENERICO) }
                    onResultado(false)
                }
        }
    }

    /** Busca o serviço completo (usado para preencher o formulário de edição). */
    fun buscarServico(id: Long, onResultado: (ServicoRS?) -> Unit) {
        viewModelScope.launch {
            repository.buscarServico(id)
                .onSuccess { onResultado(it) }
                .onFailure { erro ->
                    _estado.update { it.copy(erro = erro.message ?: ERRO_GENERICO) }
                    onResultado(null)
                }
        }
    }

    fun excluirServico(id: Long) {
        _estado.update { atual -> atual.copy(servicos = atual.servicos.filterNot { it.id == id }) }
        viewModelScope.launch {
            repository.desativarServico(id)
                .onFailure { erro ->
                    _estado.update { it.copy(erro = erro.message ?: ERRO_GENERICO) }
                    carregar()
                }
        }
    }

    fun limparErro() {
        _estado.update { it.copy(erro = null) }
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}
