package com.winyc.elo.backend.viewModel

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.auth.AuthRepository
import com.winyc.elo.backend.model.UsuarioRQ
import com.winyc.elo.backend.model.enums.CadastroAcao
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado da tela de autenticação, observado pela UI Compose. */
sealed interface AuthEstado {
    data object Ocioso : AuthEstado
    data object Carregando : AuthEstado
    data class Sucesso(val usuarioId: Long) : AuthEstado
    data class Erro(val mensagem: String) : AuthEstado
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = AuthRepository(tokenStore = tokenStore)

    // Identificador do dispositivo exigido pelo backend no login.
    private val deviceCode: String = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()

    private val _estado = MutableStateFlow<AuthEstado>(AuthEstado.Ocioso)
    val estado: StateFlow<AuthEstado> = _estado.asStateFlow()

    val estaLogado: Boolean get() = tokenStore.estaLogado

    fun login(email: String, senha: String) {
        if (_estado.value is AuthEstado.Carregando) return
        _estado.value = AuthEstado.Carregando
        viewModelScope.launch {
            repository.login(email.trim(), senha, deviceCode)
                .onSuccess { _estado.value = AuthEstado.Sucesso(it.id) }
                .onFailure { _estado.value = AuthEstado.Erro(it.message ?: ERRO_GENERICO) }
        }
    }

    fun cadastrar(
        nome: String,
        sobrenome: String,
        telefone: String,
        email: String,
        senha: String,
        comoProfissional: Boolean,
    ) {
        if (_estado.value is AuthEstado.Carregando) return
        _estado.value = AuthEstado.Carregando

        val usuario = UsuarioRQ(
            nome = nome,
            sobrenome = sobrenome,
            email = email.trim(),
            telContato = telefone.filter { it.isDigit() },
            senha = senha,
            isDuplicarTel = true,
            cadastroAcao = if (comoProfissional) {
                CadastroAcao.CADASTRAR_PROFISSIONAL
            } else {
                CadastroAcao.CADASTRAR_USUARIO
            },
        )

        viewModelScope.launch {
            repository.cadastrar(usuario, deviceCode)
                .onSuccess { _estado.value = AuthEstado.Sucesso(it.id) }
                .onFailure { _estado.value = AuthEstado.Erro(it.message ?: ERRO_GENERICO) }
        }
    }

    /** Volta ao estado ocioso depois que a UI exibiu o erro. */
    fun limparErro() {
        _estado.update { if (it is AuthEstado.Erro) AuthEstado.Ocioso else it }
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}