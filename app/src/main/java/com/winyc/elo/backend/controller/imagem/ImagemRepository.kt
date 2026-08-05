package com.winyc.elo.backend.controller.imagem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.google.gson.Gson
import com.winyc.elo.backend.model.ApiError
import com.winyc.elo.backend.model.imagem.EscopoImagem
import com.winyc.elo.backend.model.imagem.ImagemUploadRS
import com.winyc.elo.backend.retroFit.RetroFitService
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import androidx.core.graphics.scale

class ImagemRepository(
    context: Context,
    private val tokenStore: TokenStore,
    private val api: ImagemInterface = RetroFitService.imagemApi(tokenStore),
) {
    private val appContext = context.applicationContext

    suspend fun enviar(escopo: EscopoImagem, uri: Uri): Result<ImagemUploadRS> =
        executar { verificaErro(api.enviar(escopo.prefixo, prepararParte(uri)).execute()) }

    private fun prepararParte(uri: Uri): MultipartBody.Part {
        val resolver = appContext.contentResolver
        val original = ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.setTargetSampleSize(amostragem(info.size.width, info.size.height))
        }
        val reduzido = reduzir(original)

        val png = resolver.getType(uri) == MIME_PNG
        val saida = ByteArrayOutputStream()
        val formato = if (png) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        reduzido.compress(formato, QUALIDADE, saida)
        if (reduzido !== original) reduzido.recycle()
        original.recycle()

        val mime = if (png) MIME_PNG else MIME_JPEG
        val nome = if (png) "imagem.png" else "imagem.jpg"
        val corpo = saida.toByteArray().toRequestBody(mime.toMediaType())
        return MultipartBody.Part.createFormData(ImagemInterface.CAMPO_ARQUIVO, nome, corpo)
    }

    private fun amostragem(largura: Int, altura: Int): Int {
        val maior = maxOf(largura, altura)
        var amostra = 1
        while (maior / (amostra * 2) >= LADO_MAXIMO) amostra *= 2
        return amostra
    }

    private fun reduzir(bitmap: Bitmap): Bitmap {
        val maior = maxOf(bitmap.width, bitmap.height)
        if (maior <= LADO_MAXIMO) return bitmap
        val escala = LADO_MAXIMO.toFloat() / maior
        return bitmap.scale(
            (bitmap.width * escala).toInt().coerceAtLeast(1),
            (bitmap.height * escala).toInt().coerceAtLeast(1),
        )
    }

    private suspend fun <T> executar(bloco: () -> T): Result<T> =
        withContext(Dispatchers.IO) { runCatching(bloco) }.recoverCatching { erro -> throw IllegalStateException(mensagemDeFalha(erro)) }

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
        400 -> apiError?.message ?: "Imagem inválida. Envie um JPG, PNG ou WEBP."
        401, 403 -> apiError?.message ?: "Entre na sua conta para continuar."
        413 -> apiError?.message ?: "A imagem é muito grande. Escolha outra."
        415 -> apiError?.message ?: "Formato não aceito. Envie um JPG, PNG ou WEBP."
        in 500..599 -> "Servidor indisponível. Tente novamente em instantes."
        else -> apiError?.message ?: "Não foi possível enviar a imagem. Verifique sua conexão."
    }

    private fun mensagemDeFalha(erro: Throwable): String = when (erro) {
        is ImageDecoder.DecodeException, is SecurityException -> "Não foi possível ler a imagem escolhida."
        is OutOfMemoryError -> "A imagem é muito grande. Escolha outra."
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is UnknownHostException, is ConnectException -> "Sem conexão com o servidor. Verifique sua internet."
        is IOException -> "Falha de conexão. Tente novamente."
        else -> erro.message ?: "Algo deu errado. Tente novamente."
    }

    private companion object {
        const val LADO_MAXIMO = 1600
        const val QUALIDADE = 85
        const val MIME_JPEG = "image/jpeg"
        const val MIME_PNG = "image/png"
    }
}
