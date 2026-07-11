package com.winyc.elo.telas.profissional

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.winyc.elo.telas.TelaVazia

/** Painel Pro: ganhos, avaliações e resumo de desempenho. */
@Composable
fun PainelScreen(modifier: Modifier = Modifier) {
    TelaVazia(titulo = "Painel", icone = Icons.Outlined.GridView, modifier = modifier)
}
