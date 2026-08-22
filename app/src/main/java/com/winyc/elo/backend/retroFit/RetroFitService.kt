package com.winyc.elo.backend.retroFit

import com.winyc.elo.BuildConfig
import com.winyc.elo.backend.controller.auth.AuthInterface
import com.winyc.elo.backend.controller.busca.BuscaInterface
import com.winyc.elo.backend.controller.categoria.CategoriaInterface
import com.winyc.elo.backend.controller.estimativa.EstimativaInterface
import com.winyc.elo.backend.controller.imagem.ImagemInterface
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

    /** Ligue para ver o JSON completo no logcat durante uma investigação. */
    private const val LOG_CORPO_HTTP = false

    val authApi: AuthInterface by lazy { authRetrofit.create(AuthInterface::class.java) }

    private fun OkHttpClient.Builder.timeoutsPadrao() = apply {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
    }

    /**
     * Imprimir o corpo de toda resposta custa caro: serializa o JSON inteiro em
     * String e joga no logcat na thread da requisição (uma página de busca passa
     * de 5 KB). Fica em `BASIC` no dia a dia; ligue [LOG_CORPO_HTTP] quando
     * precisar inspecionar payload.
     *
     * O log de corpo também imprime os headers, incluindo `Authorization`, e o
     * payload do login/refresh — por isso o interceptor só entra em debug.
     */
    private fun OkHttpClient.Builder.logSomenteEmDebug() = apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (LOG_CORPO_HTTP) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.BASIC
                    }
                },
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
    @Volatile
    private var retrofitAutenticado: Pair<TokenStore, Retrofit>? = null

    /**
     * Um único cliente autenticado por [TokenStore] (que é singleton no processo).
     * Antes cada repositório construía o seu: cada `OkHttpClient` tem pool de
     * conexões e de threads próprios, então nada era reaproveitado e toda tela
     * pagava handshake novo.
     */
    fun retrofitAutenticado(tokenStore: TokenStore): Retrofit {
        retrofitAutenticado?.let { (store, retrofit) ->
            if (store === tokenStore) return retrofit
        }
        return synchronized(this) {
            retrofitAutenticado?.takeIf { it.first === tokenStore }?.second ?: criarRetrofitAutenticado(tokenStore)
                .also { retrofitAutenticado = tokenStore to it }
        }
    }

    private fun criarRetrofitAutenticado(tokenStore: TokenStore): Retrofit {
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

    fun imagemApi(tokenStore: TokenStore): ImagemInterface =
        retrofitAutenticado(tokenStore).create(ImagemInterface::class.java)

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