package com.winyc.elo.backend.controller.imagem

import com.winyc.elo.backend.model.imagem.ImagemUploadRS
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ImagemInterface {

    companion object {
        private const val PATH = "imagem/"
        const val CAMPO_ARQUIVO = "arquivo"
    }

    @Multipart
    @POST(PATH + "{escopo}")
    fun enviar(
        @Path("escopo") escopo: String,
        @Part arquivo: MultipartBody.Part,
    ): Call<ImagemUploadRS>
}
