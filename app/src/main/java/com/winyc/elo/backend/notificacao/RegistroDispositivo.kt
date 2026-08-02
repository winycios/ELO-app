package com.winyc.elo.backend.notificacao

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.core.content.edit
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.winyc.elo.BuildConfig
import com.winyc.elo.backend.controller.notificacao.NotificacaoRepository
import com.winyc.elo.backend.model.enums.Plataforma
import com.winyc.elo.backend.model.enums.TipoIdentificador
import com.winyc.elo.backend.model.notificacao.DispositivoRQ
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object RegistroDispositivo {

    private val mutex = Mutex()

    suspend fun sincronizar(context: Context, forcar: Boolean = false, registrarNoFcm: Boolean = true, ): Result<Unit> = mutex.withLock {
        enviar(context.applicationContext, forcar, registrarNoFcm)
    }

    suspend fun desativar(context: Context): Result<Unit> = mutex.withLock {
        remover(context.applicationContext)
    }

    fun esquecer(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { remove(KEY_ASSINATURA) }
    }

    private suspend fun remover(appContext: Context): Result<Unit> {
        val tokenStore = TokenStore.getInstance(appContext)
        if (!tokenStore.estaLogado) return Result.success(Unit)

        return NotificacaoRepository(tokenStore)
            .desativarDispositivo(codigoDispositivo(appContext))
            .onSuccess {
                esquecer(appContext)
                Log.i(TAG, "Aparelho desativado para o usuário ${tokenStore.usuarioId}.")
            }
            .onFailure { Log.w(TAG, "Falha ao desativar o aparelho: ${it.message}") }
    }

    private suspend fun enviar(appContext: Context, forcar: Boolean, registrarNoFcm: Boolean): Result<Unit> {
        val tokenStore = TokenStore.getInstance(appContext)
        if (!tokenStore.carregada) tokenStore.carregar()

        if (!tokenStore.estaLogado) return Result.success(Unit)

        if (FirebaseApp.getApps(appContext).isEmpty()) {
            Log.w(TAG, "Firebase não inicializado (google-services.json ausente?): push desligado.")
            return Result.success(Unit)
        }

        if (registrarNoFcm) garantirRegistroNoFcm()

        val fid = runCatching { obterFid() }.getOrElse { erro ->
            Log.w(TAG, "Não foi possível obter o FID.", erro)
            return Result.failure(erro)
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "FID desta instalação: $fid")

        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val assinatura = "${tokenStore.usuarioId}:$fid"
        if (!forcar && prefs.getString(KEY_ASSINATURA, null) == assinatura) {
            return Result.success(Unit)
        }

        val dispositivo = DispositivoRQ(
            codigoDispositivo = codigoDispositivo(appContext),
            identificadorFcm = fid,
            tipoIdentificador = TipoIdentificador.FID,
            plataforma = Plataforma.ANDROID,
        )

        return NotificacaoRepository(tokenStore)
            .registrarDispositivo(dispositivo)
            .onSuccess {
                prefs.edit { putString(KEY_ASSINATURA, assinatura) }
                Log.i(TAG, "Aparelho registrado para o usuário ${tokenStore.usuarioId}.")
            }
            .onFailure { Log.w(TAG, "Falha ao registrar o aparelho: ${it.message}") }
    }

    private suspend fun garantirRegistroNoFcm() {
        withContext(Dispatchers.IO) {
            runCatching {
                Tasks.await(FirebaseMessaging.getInstance().register(), TIMEOUT_FID_SEGUNDOS, TimeUnit.SECONDS)
            }.onFailure { Log.w(TAG, "Inscrição no FCM falhou; a entrega pode não ocorrer.", it) }
        }
    }

    private suspend fun obterFid(): String = withContext(Dispatchers.IO) {
        Tasks.await(FirebaseInstallations.getInstance().id, TIMEOUT_FID_SEGUNDOS, TimeUnit.SECONDS)
    }

    private fun codigoDispositivo(context: Context): String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()

    private const val TAG = "RegistroDispositivo"
    private const val PREFS = "elo_push"
    private const val KEY_ASSINATURA = "assinatura_registro"
    private const val TIMEOUT_FID_SEGUNDOS = 20L
}
