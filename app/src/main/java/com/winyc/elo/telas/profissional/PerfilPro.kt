package com.winyc.elo.telas.profissional

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.winyc.elo.telas.TelaVazia

/** Perfil profissional: dados públicos, portfólio e configurações. */
@Composable
fun PerfilProScreen(modifier: Modifier = Modifier) {
    TelaVazia(titulo = "Perfil Pro", icone = Icons.Outlined.ManageAccounts, modifier = modifier)
}
