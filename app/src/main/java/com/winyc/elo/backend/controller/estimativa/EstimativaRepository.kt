package com.winyc.elo.backend.controller.estimativa

import com.google.gson.Gson
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.estimativa.ProfissionalServicoRS
import com.winyc.elo.backend.retroFit.RetroFitService
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Perfil do profissional para estimativa. Passa pelo Retrofit autenticado: envia
 * o token quando o cliente está logado (para o backend calcular distância) e
 * segue sem ele quando deslogado.
 */
class EstimativaRepository(
    tokenStore: TokenStore,
    private val api: EstimativaInterface = RetroFitService.estimativaApi(tokenStore),
) {
    suspend fun buscarPorServico(profissionalId: Long, servicoId: Long): Result<ProfissionalServicoRS> =
        executar { verificaErro(api.buscarPorServico(profissionalId, servicoId).execute()) }

    suspend fun buscarPorCategoria(profissionalId: Long, categoriaId: Long): Result<ProfissionalServicoRS> =
        executar { verificaErro(api.buscarPorCategoria(profissionalId, categoriaId).execute()) }

    private suspend fun <T> executar(bloco: () -> T): Result<T> =
        withContext(Dispatchers.IO) { runCatching(bloco) }
            .recoverCatching { erro -> throw IllegalStateException(mensagemDeFalha(erro)) }

    private fun <T> verificaErro(resposta: Response<T>): T {
        val corpo = resposta.body()
        if (!resposta.isSuccessful || corpo == null) {
            error(mensagemDeErro(resposta.code(), lerErro(resposta)))
        }
        return corpo
    }

    private fun lerErro(resposta: Response<*>): ApiError? =
        resposta.errorBody()?.charStream()
            ?.use { reader -> runCatching { Gson().fromJson(reader, ApiError::class.java) }.getOrNull() }

    private fun mensagemDeErro(codigo: Int, apiError: ApiError?): String = when (codigo) {
        404 -> apiError?.message ?: "Profissional não encontrado."
        in 500..599 -> "Servidor indisponível. Tente novamente em instantes."
        else -> apiError?.message ?: "Não foi possível carregar o perfil. Verifique sua conexão."
    }

    private fun mensagemDeFalha(erro: Throwable): String = when (erro) {
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is UnknownHostException, is ConnectException -> "Sem conexão com o servidor. Verifique sua internet."
        is IOException -> "Falha de conexão. Tente novamente."
        else -> erro.message ?: "Algo deu errado. Tente novamente."
    }
}
