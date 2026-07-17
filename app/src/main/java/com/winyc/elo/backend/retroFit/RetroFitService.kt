package com.winyc.elo.backend.retroFit

import com.winyc.elo.backend.controller.auth.AuthInterface
import com.winyc.elo.backend.controller.categoria.CategoriaInterface
import com.winyc.elo.backend.controller.categoria.CategoriaRepository
import com.winyc.elo.backend.controller.usuario.UsuarioInterface
import com.winyc.elo.backend.controller.viacep.ViaCepInterface
import com.winyc.elo.backend.controller.vitrine.VitrineInterface
import com.winyc.elo.backend.security.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetroFitService {

    private const val BASE_URL = "http://192.168.15.57:8090/api/"
    private const val VIA_CEP_URL = "https://viacep.com.br/ws/"

    val authApi: AuthInterface by lazy { authRetrofit.create(AuthInterface::class.java) }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun OkHttpClient.Builder.timeoutsPadrao() = apply {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
    }

    // Endpoints sem auth

    private val authClient: OkHttpClient = OkHttpClient.Builder()
        .timeoutsPadrao()
        .addInterceptor(logging)
        .build()
    private val authRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(authClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Endpoints com auth
    fun retrofitAutenticado(tokenStore: TokenStore): Retrofit {
        val client = OkHttpClient.Builder()
            .timeoutsPadrao()
            .addInterceptor(AuthInterceptor(tokenStore))
            .authenticator(TokenAuthenticator(tokenStore, authApi))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun vitrineApi(tokenStore: TokenStore): VitrineInterface =
        retrofitAutenticado(tokenStore).create(VitrineInterface::class.java)

    fun usuarioApi(tokenStore: TokenStore): UsuarioInterface =
        retrofitAutenticado(tokenStore).create(UsuarioInterface::class.java)

    fun authConnection(): AuthInterface = authApi

    fun categoriaApi(): CategoriaInterface = authRetrofit.create(CategoriaInterface::class.java)

    private val viaCepRetrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .timeoutsPadrao()
            .addInterceptor(logging)
            .build()
        Retrofit.Builder()
            .baseUrl(VIA_CEP_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun viaCepApi(): ViaCepInterface = viaCepRetrofit.create(ViaCepInterface::class.java)
}