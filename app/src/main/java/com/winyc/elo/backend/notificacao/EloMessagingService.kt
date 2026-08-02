package com.winyc.elo.backend.notificacao

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EloMessagingService : FirebaseMessagingService() {

    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onRegistered(identificador: String) {
        escopo.launch {
            RegistroDispositivo.sincronizar(applicationContext, forcar = true, registrarNoFcm = false)
        }
    }

    override fun onUnregistered(identificador: String) {
        Log.i(TAG, "Instalação removida do FCM; o próximo login registra de novo.")
        RegistroDispositivo.esquecer(applicationContext)
    }

    override fun onMessageReceived(mensagem: RemoteMessage) {
        val aviso = AvisoPush.de(mensagem) ?: return
        Log.d(TAG, "Push em foreground: tipo=${aviso.tipo} rota=${aviso.rota}")
        PushBus.publicar(aviso)
    }

    private companion object {
        const val TAG = "EloMessagingService"
    }
}
