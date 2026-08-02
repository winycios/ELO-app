package com.winyc.elo.backend.retroFit

import com.winyc.elo.BuildConfig
import com.winyc.elo.backend.controller.auth.AuthInterface
import com.winyc.elo.backend.controller.busca.BuscaInterface
import com.winyc.elo.backend.controller.categoria.CategoriaInterface
import com.winyc.elo.backend.controller.estimativa.EstimativaInterface
import com.winyc.elo.backend.controller.notificacao.NotificacaoInterface
import com.winyc.elo.backend.controller.orcamento.OrcamentoInterface
import com.winyc.elo.backend.controller.profissional.ProfissionalInterface
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

    /** Vem do `buildConfigField` por buildType (veja `local.properties`). */
    private val BASE_URL = BuildConfig.BASE_URL
    private const val VIA_CEP_URL = "https://viacep.com.br/ws/"

    val authApi: AuthInterface by lazy { authRetrofit.create(AuthInterface::class.java) }

    private fun OkHttpClient.Builder.timeoutsPadrao() = apply {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
    }

    /**
     * O log de corpo imprime os headers, incluindo `Authorization`, e o payload
     * do login/refresh — por isso o interceptor só entra no build de debug.
     */
    private fun OkHttpClient.Builder.logSomenteEmDebug() = apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
            )
        }
    }

    // Endpoints sem auth

    private val authClient: OkHttpClient = OkHttpClient.Builder()
        .timeoutsPadrao()
        .logSomenteEmDebug()
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
            .logSomenteEmDebug()
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

    fun profissionalApi(tokenStore: TokenStore): ProfissionalInterface =
        retrofitAutenticado(tokenStore).create(ProfissionalInterface::class.java)

    fun orcamentoApi(tokenStore: TokenStore): OrcamentoInterface =
        retrofitAutenticado(tokenStore).create(OrcamentoInterface::class.java)

    fun notificacaoApi(tokenStore: TokenStore): NotificacaoInterface =
        retrofitAutenticado(tokenStore).create(NotificacaoInterface::class.java)

    // Público, mas usa o token quando presente (para calcular a distância).
    fun estimativaApi(tokenStore: TokenStore): EstimativaInterface =
        retrofitAutenticado(tokenStore).create(EstimativaInterface::class.java)

    fun authConnection(): AuthInterface = authApi

    fun categoriaApi(): CategoriaInterface = authRetrofit.create(CategoriaInterface::class.java)

    // Endpoint público: funciona logado ou deslogado, sem enviar token.
    fun buscaApi(): BuscaInterface = authRetrofit.create(BuscaInterface::class.java)

    private val viaCepRetrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .timeoutsPadrao()
            .logSomenteEmDebug()
            .build()
        Retrofit.Builder()
            .baseUrl(VIA_CEP_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun viaCepApi(): ViaCepInterface = viaCepRetrofit.create(ViaCepInterface::class.java)
}