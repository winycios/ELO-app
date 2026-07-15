package com.winyc.elo.backend.controller.vitrine

import com.google.gson.Gson
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.vitrine.ComentarioCreateRQ
import com.winyc.elo.backend.model.vitrine.ComentarioRS
import com.winyc.elo.backend.model.vitrine.PublicacaoFeedRS
import com.winyc.elo.backend.retroFit.RetroFitService
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class VitrineRepository(
    private val tokenStore: TokenStore,
    private val api: VitrineInterface = RetroFitService.vitrineApi(tokenStore),
) {
    suspend fun listarFeed(categoriaId: Long?, cursor: String?): Result<CursorPageRS<PublicacaoFeedRS>> =
        executar { verificaErro(api.listarFeed(categoriaId, cursor).execute()) }

    suspend fun listarComentarios(publicacaoId: Long, cursor: String?): Result<CursorPageRS<ComentarioRS>> =
        executar { verificaErro(api.listarComentarios(publicacaoId, cursor).execute()) }

    suspend fun curtir(publicacaoId: Long): Result<Unit> =
        executar { conferir(api.curtir(publicacaoId).execute()) }

    suspend fun descurtir(publicacaoId: Long): Result<Unit> =
        executar { conferir(api.descurtir(publicacaoId).execute()) }

    suspend fun comentar(publicacaoId: Long, texto: String): Result<ComentarioRS> =
        executar { verificaErro(api.comentar(publicacaoId, ComentarioCreateRQ(texto = texto)).execute()) }

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
        401, 403 -> apiError?.message ?: "Entre na sua conta para continuar."
        404 -> apiError?.message ?: "Publicação não encontrada."
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
