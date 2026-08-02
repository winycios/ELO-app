package com.winyc.elo.backend.controller.notificacao

import com.google.gson.Gson
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.notificacao.DispositivoRQ
import com.winyc.elo.backend.retroFit.RetroFitService
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NotificacaoRepository(
    private val tokenStore: TokenStore,
    private val api: NotificacaoInterface = RetroFitService.notificacaoApi(tokenStore),
) {
    suspend fun registrarDispositivo(dto: DispositivoRQ): Result<Unit> =
        executar { conferir(api.registrarDispositivo(dto).execute()) }

    suspend fun desativarDispositivo(codigoDispositivo: String): Result<Unit> =
        executar { conferir(api.desativarDispositivo(codigoDispositivo).execute()) }

    private suspend fun <T> executar(bloco: () -> T): Result<T> =
        withContext(Dispatchers.IO) { runCatching(bloco) }.recoverCatching { erro -> throw IllegalStateException(mensagemDeFalha(erro)) }

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
