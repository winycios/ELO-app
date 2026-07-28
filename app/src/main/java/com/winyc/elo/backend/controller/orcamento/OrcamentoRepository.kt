package com.winyc.elo.backend.controller.orcamento

import com.google.gson.Gson
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.orcamento.HorariosDisponiveisRS
import com.winyc.elo.backend.model.orcamento.OrcamentoCreateRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheRS
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemRS
import com.winyc.elo.backend.model.orcamento.OrcamentoRS
import com.winyc.elo.backend.retroFit.RetroFitService
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class OrcamentoRepository(tokenStore: TokenStore, private val api: OrcamentoInterface = RetroFitService.orcamentoApi(tokenStore)) {
    suspend fun buscarHorariosDisponiveis(
        servicoId: Long,
        dataReferencia: String? = null,
    ): Result<HorariosDisponiveisRS> =
        executar { verificaErro(api.buscarHorariosDisponiveis(servicoId, dataReferencia).execute()) }

    suspend fun solicitarOrcamento(request: OrcamentoCreateRQ): Result<OrcamentoRS> =
        executar { verificaErro(api.solicitarOrcamento(request).execute()) }

    suspend fun listarOrcamentos(
        status: String?,
        cursor: String?,
        tamanho: Int,
    ): Result<CursorPageRS<OrcamentoListagemRS>> = executar {
        val resposta = api.listarOrcamentos(status, cursor, tamanho).execute()
        if (resposta.code() == 204) CursorPageRS(items = emptyList(), nextCursor = null, hasNext = false)
        else verificaErro(resposta)
    }

    suspend fun buscarOrcamentoPorId(orcamentoId: Long): Result<OrcamentoDetalheRS> =
        executar { verificaErro(api.buscarOrcamentoPorId(orcamentoId).execute()) }

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
        400 -> apiError?.message ?: "Verifique os dados informados e tente novamente."
        401, 403 -> apiError?.message ?: "Entre na sua conta para continuar."
        404 -> apiError?.message ?: "Serviço não encontrado."
        409 -> apiError?.message ?: "Este horário não está mais disponível. Escolha outro."
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
