package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.usuario.UsuarioRepository
import com.winyc.elo.backend.model.endereco.EnderecoCreateRQ
import com.winyc.elo.backend.model.endereco.EnderecoRS
import com.winyc.elo.backend.model.usuario.UsuarioEditRQ
import com.winyc.elo.backend.model.usuario.UsuarioRS
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsuarioUi(
    val perfil: UsuarioRS? = null,
    val enderecos: List<EnderecoRS> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
) {
    val principal: EnderecoRS?
        get() = enderecos.firstOrNull { it.stPrincipal == true } ?: enderecos.firstOrNull()
}

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = UsuarioRepository(tokenStore)

    private val _estado = MutableStateFlow(UsuarioUi())
    val estado: StateFlow<UsuarioUi> = _estado.asStateFlow()

    init {
        if (tokenStore.estaLogado) carregar()
        observarSessao()
    }

    private fun observarSessao() {
        viewModelScope.launch {
            tokenStore.estaLogadoFlow.drop(1).collect { logado ->
                if (logado) {
                    _estado.value = UsuarioUi()
                    carregar()
                } else {
                    _estado.value = UsuarioUi()
                }
            }
        }
    }

    fun carregar() {
        if (!tokenStore.estaLogado || _estado.value.carregando) return
        _estado.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            val perfil = repository.pegarPerfil()
            val enderecos = repository.listarEnderecos()
            _estado.update { atual ->
                atual.copy(
                    perfil = perfil.getOrNull() ?: atual.perfil,
                    enderecos = enderecos.getOrDefault(atual.enderecos),
                    carregando = false,
                    erro = (perfil.exceptionOrNull() ?: enderecos.exceptionOrNull())?.message,
                )
            }
        }
    }

    fun editarPerfil(
        nome: String,
        sobrenome: String,
        email: String,
        telContato: String,
        telContatoZap: String,
        onResultado: (Boolean) -> Unit = {},
    ) {
        if (_estado.value.salvando) return
        _estado.update { it.copy(salvando = true, erro = null) }
        val dto = UsuarioEditRQ(
            id = _estado.value.perfil?.id,
            nome = nome.trim(),
            sobrenome = sobrenome.trim(),
            email = email.trim(),
            telContato = telContato.filter { it.isDigit() },
            telContatoZap = telContatoZap.filter { it.isDigit() }.ifBlank { telContato.filter { it.isDigit() } },
        )
        viewModelScope.launch {
            repository.editarPerfil(dto)
                .onSuccess { atualizado ->
                    _estado.update { it.copy(perfil = atualizado, salvando = false) }
                    onResultado(true)
                }
                .onFailure { erro ->
                    _estado.update { it.copy(salvando = false, erro = erro.message ?: ERRO_GENERICO) }
                    onResultado(false)
                }
        }
    }

    fun salvarEndereco(dto: EnderecoCreateRQ, onResultado: (Boolean) -> Unit = {}) {
        if (_estado.value.salvando) return
        _estado.update { it.copy(salvando = true, erro = null) }
        viewModelScope.launch {
            repository.salvarEndereco(dto)
                .onSuccess {
                    _estado.update { it.copy(salvando = false) }
                    recarregarEnderecos()
                    onResultado(true)
                }
                .onFailure { erro ->
                    _estado.update { it.copy(salvando = false, erro = erro.message ?: ERRO_GENERICO) }
                    onResultado(false)
                }
        }
    }

    fun definirPrincipal(id: Long) {
        _estado.update { atual ->
            atual.copy(enderecos = atual.enderecos.map { it.copy(stPrincipal = it.id == id) })
        }
        viewModelScope.launch {
            repository.definirPrincipal(id)
                .onSuccess { recarregarEnderecos() }
                .onFailure { erro ->
                    _estado.update { it.copy(erro = erro.message ?: ERRO_GENERICO) }
                    recarregarEnderecos()
                }
        }
    }

    fun excluirEndereco(id: Long) {
        _estado.update { atual -> atual.copy(enderecos = atual.enderecos.filterNot { it.id == id }) }
        viewModelScope.launch {
            repository.desativarEndereco(id)
                .onFailure { erro ->
                    _estado.update { it.copy(erro = erro.message ?: ERRO_GENERICO) }
                    recarregarEnderecos()
                }
        }
    }

    fun limparErro() {
        _estado.update { it.copy(erro = null) }
    }

    private fun recarregarEnderecos() {
        viewModelScope.launch {
            repository.listarEnderecos().onSuccess { lista ->
                _estado.update { it.copy(enderecos = lista) }
            }
        }
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}
