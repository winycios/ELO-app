package com.winyc.elo.telas.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val VerdeSucesso = Color(0xFF12A15A)

enum class TipoAviso { Sucesso, Erro }

@Composable
fun ToastAviso(
    mensagem: String?,
    modifier: Modifier = Modifier,
    tipo: TipoAviso = TipoAviso.Erro,
) {
    var ultimaMensagem by remember { mutableStateOf("") }
    if (!mensagem.isNullOrBlank()) ultimaMensagem = mensagem

    val fundo = when (tipo) {
        TipoAviso.Sucesso -> VerdeSucesso
        TipoAviso.Erro -> MaterialTheme.colorScheme.errorContainer
    }
    val conteudo = when (tipo) {
        TipoAviso.Sucesso -> Color.White
        TipoAviso.Erro -> MaterialTheme.colorScheme.onErrorContainer
    }
    val icone = when (tipo) {
        TipoAviso.Sucesso -> Icons.Outlined.CheckCircle
        TipoAviso.Erro -> Icons.Outlined.ErrorOutline
    }

    AnimatedVisibility(
        visible = !mensagem.isNullOrBlank(),
        enter = slideInVertically(tween(260)) { -it } + fadeIn(tween(260)),
        exit = slideOutVertically(tween(220)) { -it } + fadeOut(tween(220)),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(fundo)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = conteudo,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = ultimaMensagem,
                style = MaterialTheme.typography.bodyMedium,
                color = conteudo,
            )
        }
    }
}

const val DURACAO_AVISO_MS = 2800L
