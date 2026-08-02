package com.winyc.elo.backend.notificacao

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.google.firebase.messaging.RemoteMessage
import com.winyc.elo.R
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class AvisoPush(val titulo: String, val corpo: String, val tipo: String, val rota: String, val orcamentoId: Long?, val notificacaoId: Long?, ) {
    companion object {
        fun de(mensagem: RemoteMessage): AvisoPush? {
            val dados = mensagem.data
            val titulo = mensagem.notification?.title ?: dados["titulo"].orEmpty()
            val corpo = mensagem.notification?.body ?: dados["corpo"].orEmpty()
            if (titulo.isBlank() && corpo.isBlank()) return null

            return AvisoPush(
                titulo = titulo,
                corpo = corpo,
                tipo = dados["tipo"].orEmpty(),
                rota = dados["rota"].orEmpty(),
                orcamentoId = dados["orcamentoId"]?.toLongOrNull(),
                notificacaoId = dados["notificacaoId"]?.toLongOrNull(),
            )
        }
    }
}

object PushBus {

    private val _avisos = MutableSharedFlow<AvisoPush>(
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val avisos: SharedFlow<AvisoPush> = _avisos.asSharedFlow()

    fun publicar(aviso: AvisoPush) {
        _avisos.tryEmit(aviso)
    }
}

object CanalNotificacao {

    fun garantir(context: Context) {
        val gerenciador = context.getSystemService<NotificationManager>() ?: return
        val id = context.getString(R.string.notificacao_canal_id)
        if (gerenciador.getNotificationChannel(id) != null) return

        gerenciador.createNotificationChannel(
            NotificationChannel(
                id,
                context.getString(R.string.notificacao_canal_nome),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notificacao_canal_descricao)
            },
        )
    }
}
