package com.winyc.elo.telas.profissional

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.winyc.elo.telas.TelaVazia

/** Orçamentos recebidos e respostas do profissional. */
@Composable
fun OrcamentosScreen(modifier: Modifier = Modifier) {
    TelaVazia(titulo = "Orçamentos", icone = Icons.Outlined.Description, modifier = modifier)
}
