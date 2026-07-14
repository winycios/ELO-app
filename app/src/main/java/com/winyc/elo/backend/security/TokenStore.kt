package com.winyc.elo.backend.security

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.winyc.elo.backend.model.AuthRS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.security.GeneralSecurityException

private val Context.sessaoDataStore: DataStore<Preferences> by preferencesDataStore(name = "elo_sessao")

/**
 * Dados de perfil da sessão logada, observáveis pela UI.
 */
data class PerfilSessao(
    val id: Long,
    val nome: String,
    val urlPerfil: String,
    val urlPerfilPro: String,
    val clienteAtivo: Boolean,
    val profissionalAtivo: Boolean,
)

/**
 * Guarda a sessão em [DataStore] com os valores cifrados por AEAD (Tink), cuja
 * chave-mestra fica no Android Keystore.
 */

class TokenStore private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore = appContext.sessaoDataStore
    private val aead: Aead = criarAead(appContext)
    @Volatile
    private var accessCache: String? = null
    @Volatile
    private var refreshCache: String? = null
    @Volatile
    private var usuarioIdCache: Long = SEM_USUARIO
    @Volatile
    private var nomeCache: String = ""
    @Volatile
    private var urlPerfilCache: String = ""
    @Volatile
    private var urlPerfilProCache: String = ""
    @Volatile
    private var clienteAtivoCache: Boolean = false
    @Volatile
    private var profAtivoCache: Boolean = false

    private val _estaLogado = MutableStateFlow(false)
    private val _perfil = MutableStateFlow<PerfilSessao?>(null)

    val estaLogadoFlow: StateFlow<Boolean> = _estaLogado.asStateFlow()
    val perfilFlow: StateFlow<PerfilSessao?> = _perfil.asStateFlow()

    val accessToken: String? get() = accessCache
    val refreshToken: String? get() = refreshCache
    val usuarioId: Long get() = usuarioIdCache
    val estaLogado: Boolean get() = _estaLogado.value

    /** Lê a sessão persistida e popula o cache. Chame uma vez no start do app. */
    suspend fun carregar() {
        val prefs = dataStore.data.first()
        accessCache = prefs[KEY_ACCESS]?.let(::decifrar)
        refreshCache = prefs[KEY_REFRESH]?.let(::decifrar)
        usuarioIdCache = prefs[KEY_USUARIO_ID] ?: SEM_USUARIO
        nomeCache = prefs[KEY_NOME]?.let(::decifrar).orEmpty()
        urlPerfilCache = prefs[KEY_URL_PERFIL]?.let(::decifrar).orEmpty()
        urlPerfilProCache = prefs[KEY_URL_PERFIL_PRO]?.let(::decifrar).orEmpty()
        clienteAtivoCache = prefs[KEY_CLIENTE_ATIVO] ?: false
        profAtivoCache = prefs[KEY_PROF_ATIVO] ?: false
        atualizarEstado()
    }

    /** Persiste a sessão retornada pelo login/refresh. */
    suspend fun salvar(auth: AuthRS) {
        accessCache = auth.token
        refreshCache = auth.refreshToken
        usuarioIdCache = auth.id
        nomeCache = auth.nome
        urlPerfilCache = auth.urlPerfil.orEmpty()
        urlPerfilProCache = auth.urlPerfilPro.orEmpty()
        clienteAtivoCache = auth.isClienteAtivo
        profAtivoCache = auth.isProfissionalAtivo
        atualizarEstado()
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = cifrar(auth.token)
            prefs[KEY_REFRESH] = cifrar(auth.refreshToken)
            prefs[KEY_USUARIO_ID] = auth.id
            prefs[KEY_NOME] = cifrar(auth.nome)
            prefs[KEY_URL_PERFIL] = cifrar(urlPerfilCache)
            prefs[KEY_URL_PERFIL_PRO] = cifrar(urlPerfilProCache)
            prefs[KEY_CLIENTE_ATIVO] = auth.isClienteAtivo
            prefs[KEY_PROF_ATIVO] = auth.isProfissionalAtivo
        }
    }

    /** Apaga a sessão (logout ou refresh que falhou). */
    suspend fun limpar() {
        accessCache = null
        refreshCache = null
        usuarioIdCache = SEM_USUARIO
        nomeCache = ""
        urlPerfilCache = ""
        urlPerfilProCache = ""
        clienteAtivoCache = false
        profAtivoCache = false
        atualizarEstado()
        dataStore.edit { it.clear() }
    }

    fun salvarBloqueante(auth: AuthRS) = runBlocking { salvar(auth) }
    fun limparBloqueante() = runBlocking { limpar() }

    private fun atualizarEstado() {
        val logado = !accessCache.isNullOrBlank() && !refreshCache.isNullOrBlank()
        _estaLogado.value = logado
        _perfil.value = if (logado && usuarioIdCache != SEM_USUARIO) {
            PerfilSessao(
                id = usuarioIdCache,
                nome = nomeCache,
                urlPerfil = urlPerfilCache,
                urlPerfilPro = urlPerfilProCache,
                clienteAtivo = clienteAtivoCache,
                profissionalAtivo = profAtivoCache,
            )
        } else {
            null
        }
    }

    private fun cifrar(texto: String): String =
        Base64.encodeToString(aead.encrypt(texto.toByteArray(), AAD), Base64.NO_WRAP)

    private fun decifrar(texto: String): String? = try {
        String(aead.decrypt(Base64.decode(texto, Base64.NO_WRAP), AAD))
    } catch (_: GeneralSecurityException) {
        null
    }

    companion object {
        @Volatile
        private var instancia: TokenStore? = null

        fun getInstance(context: Context): TokenStore =
            instancia ?: synchronized(this) {
                instancia ?: TokenStore(context).also { instancia = it }
            }

        private const val SEM_USUARIO = -1L
        private val AAD = "elo-sessao".toByteArray()
        private val KEY_ACCESS = stringPreferencesKey("access_token")
        private val KEY_REFRESH = stringPreferencesKey("refresh_token")
        private val KEY_USUARIO_ID = longPreferencesKey("usuario_id")
        private val KEY_NOME = stringPreferencesKey("perfil_nome")
        private val KEY_URL_PERFIL = stringPreferencesKey("perfil_url")
        private val KEY_URL_PERFIL_PRO = stringPreferencesKey("perfil_url_pro")
        private val KEY_CLIENTE_ATIVO = booleanPreferencesKey("cliente_ativo")
        private val KEY_PROF_ATIVO = booleanPreferencesKey("prof_ativo")

        private fun criarAead(context: Context): Aead {
            AeadConfig.register()
            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(context, "elo_tink_keyset", "elo_tink_prefs")
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://elo_token_master_key")
                .build()
                .keysetHandle
            return keysetHandle.getPrimitive(Aead::class.java)
        }
    }
}
