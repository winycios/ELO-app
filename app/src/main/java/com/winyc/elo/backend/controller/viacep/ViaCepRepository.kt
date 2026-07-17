package com.winyc.elo.backend.controller.viacep

import com.winyc.elo.backend.model.endereco.ViaCepRS
import com.winyc.elo.backend.retroFit.RetroFitService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ViaCepRepository(
    private val api: ViaCepInterface = RetroFitService.viaCepApi(),
) {
    suspend fun consultar(cep: String): Result<ViaCepRS> =
        withContext(Dispatchers.IO) {
            runCatching {
                val digitos = cep.filter { it.isDigit() }
                if (digitos.length != 8) error("Informe um CEP com 8 dígitos.")

                val resposta = api.consultar(digitos).execute()
                val corpo = resposta.body()
                if (!resposta.isSuccessful || corpo == null || corpo.erro == true) {
                    error("CEP não encontrado.")
                }
                corpo
            }
        }.recoverCatching { erro -> throw IllegalStateException(mensagemDeFalha(erro)) }

    private fun mensagemDeFalha(erro: Throwable): String = when (erro) {
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is UnknownHostException, is ConnectException -> "Sem conexão. Verifique sua internet."
        is IOException -> "Falha ao consultar o CEP. Tente novamente."
        else -> erro.message ?: "Não foi possível consultar o CEP."
    }
}
