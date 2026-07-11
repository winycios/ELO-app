package com.winyc.elo.telas.profissional

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.winyc.elo.telas.TelaVazia

/** Publicação de novos serviços/anúncios pelo profissional. */
@Composable
fun PublicarScreen(modifier: Modifier = Modifier) {
    TelaVazia(titulo = "Publicar", icone = Icons.Outlined.AddBox, modifier = modifier)
}
