package com.winyc.elo.backend.controller.auth

import com.google.gson.Gson
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.AuthRQ
import com.winyc.elo.backend.model.AuthRS
import com.winyc.elo.backend.model.UsuarioRQ
import com.winyc.elo.backend.retroFit.RetroFitService
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.jvm.java

class AuthRepository(
    private val api: AuthInterface = RetroFitService.authConnection(),
    private val tokenStore: TokenStore,
) {
    suspend fun login(email: String, senha: String, deviceCode: String): Result<AuthRS> =
        withContext(Dispatchers.IO) {
            runCatching {
                val resposta =
                    api.logar(AuthRQ(email = email, senha = senha, deviceCode = deviceCode))
                        .execute()
                val corpo = resposta.body()
                if (!resposta.isSuccessful || corpo == null) {
                    val apiError = resposta.errorBody()?.charStream()
                        ?.use { reader -> Gson().fromJson(reader, ApiError::class.java) }
                    error(mensagemDeErro(resposta.code(), apiError))
                }
                tokenStore.salvar(corpo)
                corpo
            }
        }

    /**
     * Cria o usuário e, em seguida, faz login automático com as mesmas credenciais
     * para já obter os tokens.
     */
    suspend fun cadastrar(usuario: UsuarioRQ, deviceCode: String): Result<AuthRS> =
        withContext(Dispatchers.IO) {
            runCatching {
                val resposta = api.criarUsuario(usuario).execute()
                if (!resposta.isSuccessful) {
                    val apiError = resposta.errorBody()?.charStream()
                        ?.use { reader -> Gson().fromJson(reader, ApiError::class.java) }

                    error(mensagemDeErro(resposta.code(), apiError))
                }
            }.fold(
                onSuccess = { login(usuario.email, usuario.senha, deviceCode) },
                onFailure = { Result.failure(it) },
            )
        }

    suspend fun logout() = tokenStore.limpar()

    private fun mensagemDeErro(codigo: Int, apiError: ApiError?): String = when (codigo) {
        401, 403 -> apiError?.message ?: "E-mail ou senha inválidos."
        409 -> "Já existe uma conta com esse e-mail."
        in 500..599 -> "Servidor indisponível. Tente novamente em instantes."
        else -> "Não foi possível concluir. Verifique sua conexão."
    }
}