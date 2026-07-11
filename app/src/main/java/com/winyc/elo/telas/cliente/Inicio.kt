package com.winyc.elo.telas.cliente

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.winyc.elo.telas.TelaVazia

/** Home do cliente: busca de serviços, categorias e recomendações. */
@Composable
fun InicioScreen(modifier: Modifier = Modifier) {
    TelaVazia(titulo = "Início", icone = Icons.Outlined.Home, modifier = modifier)
}