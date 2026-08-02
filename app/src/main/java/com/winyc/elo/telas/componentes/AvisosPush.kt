package com.winyc.elo.telas.componentes

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.winyc.elo.R
import com.winyc.elo.backend.notificacao.AvisoPush
import com.winyc.elo.backend.notificacao.CanalNotificacao
import com.winyc.elo.backend.notificacao.PushBus
import com.winyc.elo.backend.notificacao.RegistroDispositivo
import kotlinx.coroutines.delay

private const val DURACAO_AVISO_PUSH_MS = 6_000L
private const val ENTRADA_MS = 260
private const val SAIDA_MS = 220

@Composable
fun AvisosPush(logado: Boolean, modifier: Modifier = Modifier) {
    RegistroPushEfeito(logado)
    BannerAvisoPush(modifier)
}

@Composable
private fun RegistroPushEfeito(logado: Boolean) {
    val context = LocalContext.current

    val pedirPermissao = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(),) { }

    LaunchedEffect(logado) {
        CanalNotificacao.garantir(context)

        if (!logado) {
            RegistroDispositivo.esquecer(context)
            return@LaunchedEffect
        }

        val concedida = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!concedida) pedirPermissao.launch(Manifest.permission.POST_NOTIFICATIONS)

        RegistroDispositivo.sincronizar(context)
    }
}

@Composable
private fun BannerAvisoPush(modifier: Modifier = Modifier) {
    var aviso by remember { mutableStateOf<AvisoPush?>(null) }
    var visivel by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PushBus.avisos.collect { novo ->
            aviso = novo
            visivel = true
        }
    }

    LaunchedEffect(aviso, visivel) {
        if (!visivel) return@LaunchedEffect
        delay(DURACAO_AVISO_PUSH_MS)
        visivel = false
    }

    AnimatedVisibility(
        visible = visivel,
        modifier = modifier,
        enter = slideInVertically(tween(ENTRADA_MS)) { -it } + fadeIn(tween(ENTRADA_MS)),
        exit = slideOutVertically(tween(SAIDA_MS)) { -it } + fadeOut(tween(SAIDA_MS)),
    ) {
        aviso?.let { CartaoAviso(aviso = it, onFechar = { visivel = false }) }
    }
}

@Composable
private fun CartaoAviso(aviso: AvisoPush, onFechar: () -> Unit) {
    val titulo = aviso.titulo.ifBlank { stringResource(R.string.app_name) }

    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(onClick = onFechar),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (aviso.corpo.isNotBlank()) {
                    Text(
                        text = aviso.corpo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            IconButton(onClick = onFechar) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.notificacao_fechar_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
