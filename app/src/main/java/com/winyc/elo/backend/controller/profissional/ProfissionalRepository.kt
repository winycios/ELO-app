package com.winyc.elo.backend.controller.profissional

import com.google.gson.Gson
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.servico.ServicoCreateDTO
import com.winyc.elo.backend.model.servico.ServicoListaRS
import com.winyc.elo.backend.model.servico.ServicoRS
import com.winyc.elo.backend.model.vitrine.PublicacaoCreateDTO
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

class ProfissionalRepository(
    private val tokenStore: TokenStore,
    private val api: ProfissionalInterface = RetroFitService.profissionalApi(tokenStore),
) {
    suspend fun listarServicos(): Result<List<ServicoListaRS>> =
        executar {
            val resposta = api.listarServicos().execute()
            if (resposta.code() == 204) emptyList() else verificaErro(resposta)
        }

    suspend fun buscarServico(id: Long): Result<ServicoRS> =
        executar { verificaErro(api.buscarServico(id).execute()) }

    suspend fun salvarServico(dto: ServicoCreateDTO): Result<ServicoRS> =
        executar { verificaErro(api.salvarServico(dto).execute()) }

    suspend fun desativarServico(id: Long): Result<Unit> =
        executar { conferir(api.desativarServico(id).execute()) }

    suspend fun listarMinhasPublicacoes(categoriaId: Long?, cursor: String?): Result<CursorPageRS<PublicacaoFeedRS>> =
        executar {
            val resposta = api.listarMinhasPublicacoes(categoriaId, cursor).execute()
            if (resposta.code() == 204) CursorPageRS(items = emptyList(), nextCursor = null, hasNext = false)
            else verificaErro(resposta)
        }

    suspend fun salvarPublicacao(dto: PublicacaoCreateDTO): Result<PublicacaoFeedRS> =
        executar { verificaErro(api.salvarPublicacao(dto).execute()) }

    suspend fun desativarPublicacao(id: Long): Result<Unit> =
        executar { conferir(api.desativarPublicacao(id).execute()) }

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
        404 -> apiError?.message ?: "Serviço não encontrado."
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
