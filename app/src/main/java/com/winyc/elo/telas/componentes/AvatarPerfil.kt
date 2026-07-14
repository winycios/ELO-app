package com.winyc.elo.telas.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

/**
 * Avatar circular do usuário. Mostra a [fotoUrl] quando disponível; caso contrário,
 * cai no círculo com as iniciais do [nome] (útil para contas sem foto de perfil).
 */
@Composable
fun AvatarPerfil(
    nome: String,
    fotoUrl: String?,
    tamanho: Dp,
    fonte: TextStyle,
    modifier: Modifier = Modifier,
) {
    val forma = Modifier
        .size(tamanho)
        .clip(CircleShape)

    if (fotoUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .then(forma)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = iniciaisDe(nome),
                style = fonte,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    } else {
        AsyncImage(
            model = fotoUrl,
            contentDescription = nome,
            contentScale = ContentScale.Crop,
            modifier = modifier.then(forma),
        )
    }
}

/** Até duas iniciais em maiúsculas a partir do nome completo. */
fun iniciaisDe(nome: String): String =
    nome.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
