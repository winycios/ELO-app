package com.winyc.elo.backend.controller.categoria

import com.google.gson.Gson
import com.winyc.elo.backend.controller.vitrine.VitrineInterface
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.categoria.CategoriaRS
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

class CategoriaRepository(
    private val api: CategoriaInterface = RetroFitService.categoriaApi(),
) {
    suspend fun listarCategorias(): Result<List<CategoriaRS>> =
        executar {
            val resposta = api.listarCategoria().execute()
            if (!resposta.isSuccessful) {
                throw IllegalStateException("Não foi possível carregar as categorias (${resposta.code()}).")
            }
            resposta.body() ?: emptyList()
        }

    private suspend fun <T> executar(bloco: () -> T): Result<T> =
        withContext(Dispatchers.IO) { runCatching(bloco) }.recoverCatching { erro -> throw IllegalStateException(mensagemDeFalha(erro)) }

    private fun mensagemDeFalha(erro: Throwable): String = when (erro) {
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is UnknownHostException, is ConnectException -> "Sem conexão com o servidor. Verifique sua internet."
        is IOException -> "Falha de conexão. Tente novamente."
        else -> erro.message ?: "Algo deu errado. Tente novamente."
    }
}
