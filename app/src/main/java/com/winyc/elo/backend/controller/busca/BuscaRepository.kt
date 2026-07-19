package com.winyc.elo.backend.controller.busca

import com.google.gson.Gson
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.search.BuscaProfissionalRS
import com.winyc.elo.backend.model.search.OrdenacaoBusca
import com.winyc.elo.backend.retroFit.RetroFitService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Busca de profissionais. É um endpoint público (funciona logado ou não), por
 * isso usa o Retrofit sem autenticação.
 */
class BuscaRepository(
    private val api: BuscaInterface = RetroFitService.buscaApi(),
) {
    suspend fun buscar(
        texto: String? = null,
        categoriaId: Long? = null,
        avaliacaoMinima: Double? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        distanciaKm: Double? = null,
        ordenacao: OrdenacaoBusca = OrdenacaoBusca.RECOMENDADOS,
        pagina: Int = 0,
        tamanho: Int = 20,
    ): Result<BuscaProfissionalRS> =
        executar {
            val resposta = api.buscarProfissionais(
                texto = texto?.trim()?.takeIf { it.isNotEmpty() },
                categoriaId = categoriaId,
                avaliacaoMinima = avaliacaoMinima?.takeIf { it > 0.0 },
                latitude = latitude,
                longitude = longitude,
                distanciaKm = distanciaKm,
                ordenacao = ordenacao.name,
                pagina = pagina,
                tamanho = tamanho,
            ).execute()
            if (resposta.code() == 204) BuscaProfissionalRS() else verificaErro(resposta)
        }

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
        400 -> apiError?.message ?: "Não foi possível concluir a busca."
        in 500..599 -> "Servidor indisponível. Tente novamente em instantes."
        else -> apiError?.message ?: "Não foi possível buscar. Verifique sua conexão."
    }

    private fun mensagemDeFalha(erro: Throwable): String = when (erro) {
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is UnknownHostException, is ConnectException -> "Sem conexão com o servidor. Verifique sua internet."
        is IOException -> "Falha de conexão. Tente novamente."
        else -> erro.message ?: "Algo deu errado. Tente novamente."
    }
}
