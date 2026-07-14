package spectrum.fittech.backend.log

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

//Classe usada para Habilitar logging para inspeção das requisições e respostas

val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

val client = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()
