package com.winyc.elo.backend.controller.usuario

import com.google.gson.Gson
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.endereco.EnderecoCreateRQ
import com.winyc.elo.backend.model.endereco.EnderecoRS
import com.winyc.elo.backend.model.usuario.UsuarioEditRQ
import com.winyc.elo.backend.model.usuario.UsuarioRS
import com.winyc.elo.backend.retroFit.RetroFitService
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UsuarioRepository(
    private val tokenStore: TokenStore,
    private val api: UsuarioInterface = RetroFitService.usuarioApi(tokenStore),
) {
    suspend fun pegarPerfil(): Result<UsuarioRS> =
        executar { verificaErro(api.pegarPerfil().execute()) }

    suspend fun editarPerfil(dto: UsuarioEditRQ): Result<UsuarioRS> =
        executar { verificaErro(api.editarPerfil(dto).execute()) }

    suspend fun listarEnderecos(): Result<List<EnderecoRS>> =
        executar {
            val resposta = api.listarEnderecos().execute()
            if (resposta.code() == 204) emptyList() else verificaErro(resposta)
        }

    suspend fun buscarPrincipal(): Result<EnderecoRS?> =
        executar {
            val resposta = api.buscarPrincipal().execute()
            if (resposta.code() == 204) null else verificaErro(resposta)
        }

    suspend fun salvarEndereco(dto: EnderecoCreateRQ): Result<EnderecoRS> =
        executar { verificaErro(api.salvarEndereco(dto).execute()) }

    suspend fun definirPrincipal(id: Long): Result<Unit> =
        executar { conferir(api.definirPrincipal(id).execute()) }

    suspend fun desativarEndereco(id: Long): Result<Unit> =
        executar { conferir(api.desativarEndereco(id).execute()) }

    private suspend fun <T> executar(bloco: () -> T): Result<T> =
        withContext(Dispatchers.IO) { runCatching(bloco) }.recoverCatching { erro -> throw IllegalStateException(mensagemDeFalha(erro)) }

    private fun <T> verificaErro(resposta: Response<T>): T {
        val corpo = resposta.body()
        if (!resposta.isSuccessful || corpo == null) {
            error(mensagemDeErro(resposta.code(), lerErro(resposta)))
        }
        return corpo
    }

    private fun conferir(resposta: Response<Void>) {
        if (!resposta.isSuccessful) {
            error(mensagemDeErro(resposta.code(), lerErro(resposta)))
        }
    }

    private fun lerErro(resposta: Response<*>): ApiError? =
        resposta.errorBody()?.charStream()
            ?.use { reader -> runCatching { Gson().fromJson(reader, ApiError::class.java) }.getOrNull() }

    private fun mensagemDeErro(codigo: Int, apiError: ApiError?): String = when (codigo) {
        400 -> apiError?.message ?: "Verifique os dados informados e tente novamente."
        401, 403 -> apiError?.message ?: "Entre na sua conta para continuar."
        404 -> apiError?.message ?: "Não encontrado."
        in 500..599 -> "Servidor indisponível. Tente novamente em instantes."
        else -> apiError?.message ?: "Não foi possível concluir. Verifique sua conexão."
    }

    private fun mensagemDeFalha(erro: Throwable): String = when (erro) {
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is UnknownHostException, is ConnectException -> "Sem conexão com o servidor. Verifique sua internet."
        is IOException -> "Falha de conexão. Tente novamente."
        else -> erro.message ?: "Algo deu errado. Tente novamente."
    }
}
