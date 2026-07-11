package com.winyc.elo.telas.cliente

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.winyc.elo.telas.TelaVazia

/** Perfil do cliente. */
@Composable
fun PerfilScreen(modifier: Modifier = Modifier) {
    TelaVazia(titulo = "Perfil", icone = Icons.Outlined.Person, modifier = modifier)
}
