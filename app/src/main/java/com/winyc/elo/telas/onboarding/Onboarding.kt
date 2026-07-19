package com.winyc.elo.telas.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winyc.elo.R
import com.winyc.elo.ui.theme.EloTheme
import com.winyc.elo.ui.theme.EloTintaEscura
import kotlinx.coroutines.launch

/* ============================ Modelos ============================ */

private enum class Acento { Neutro, Cliente, Profissional }

private data class ItemDestaque(val icone: ImageVector, val texto: String)

private data class PaginaOnboarding(
    val acento: Acento,
    val rotuloTopo: String,
    val titulo: String,
    val tituloComAcento: Boolean,
    val descricao: String,
    val itens: List<ItemDestaque>,
    val textoBotao: String,
)

private val PAGINAS = listOf(
    PaginaOnboarding(
        acento = Acento.Neutro,
        rotuloTopo = "Bem-vindo ao",
        titulo = "ELO",
        tituloComAcento = false,
        descricao = "O elo entre quem precisa de um serviço e quem sabe fazer. " +
            "Um só app, dois modos que trabalham juntos.",
        itens = listOf(
            ItemDestaque(Icons.Outlined.PersonOutline, "Modo Cliente — contrate com confiança"),
            ItemDestaque(Icons.Outlined.WorkOutline, "Modo Profissional — receba e feche trabalhos"),
        ),
        textoBotao = "Continuar",
    ),
    PaginaOnboarding(
        acento = Acento.Cliente,
        rotuloTopo = "Como Cliente",
        titulo = "Contrate em minutos",
        tituloComAcento = true,
        descricao = "Encontre profissionais verificados perto de você, compare " +
            "avaliações e peça orçamentos sem sair do app.",
        itens = listOf(
            ItemDestaque(Icons.Outlined.Search, "Busque por serviço ou categoria"),
            ItemDestaque(Icons.Outlined.Shield, "Profissionais verificados e bem avaliados"),
            ItemDestaque(Icons.Outlined.Description, "Peça orçamentos e contrate na hora"),
        ),
        textoBotao = "Continuar",
    ),
    PaginaOnboarding(
        acento = Acento.Profissional,
        rotuloTopo = "Como Profissional",
        titulo = "Transforme em renda",
        tituloComAcento = true,
        descricao = "Publique seus serviços, responda pedidos e acompanhe seus " +
            "ganhos — tudo pelo modo profissional.",
        itens = listOf(
            ItemDestaque(Icons.Outlined.Description, "Receba e responda orçamentos"),
            ItemDestaque(Icons.AutoMirrored.Filled.TrendingUp, "Acompanhe seus KPIs e ganhos"),
            ItemDestaque(Icons.Outlined.WorkOutline, "Alterne entre os modos quando quiser"),
        ),
        textoBotao = "Começar",
    ),
)

/* ============================ Tela ============================ */

/**
 * Onboarding de primeiro acesso: 3 telas explicando o app.
 *
 * - O fundo respeita o tema do dispositivo (claro/escuro).
 * - O acento (neutro/coral/teal) muda por página, com um brilho no topo.
 * - "Continuar" desliza para a próxima; na última, conclui.
 *
 * @param onConcluir chamado ao pular ou finalizar o onboarding.
 */
@Composable
fun OnboardingScreen(onConcluir: () -> Unit, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { PAGINAS.size })
    val escopo = rememberCoroutineScope()
    val paginaAtual = PAGINAS[pagerState.currentPage]
    val ultima = pagerState.currentPage == PAGINAS.lastIndex

    val acento by animateColorAsState(
        targetValue = corAcento(paginaAtual.acento),
        animationSpec = tween(400),
        label = "acento",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { indice ->
            ConteudoPagina(PAGINAS[indice])
        }

        // "Pular" — topo direito.
        Text(
            text = "Pular",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
                .clip(CircleShape)
                .clickable(onClick = onConcluir)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )

        // Indicador de páginas + botão — rodapé.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(PAGINAS.size) { indice ->
                    val ativo = indice == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (ativo) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (ativo) acento else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (ultima) {
                        onConcluir()
                    } else {
                        escopo.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = if (paginaAtual.acento == Acento.Neutro) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background,
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = acento,
                        contentColor = Color.White,
                    )
                },
            ) {
                Text(paginaAtual.textoBotao, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ConteudoPagina(pagina: PaginaOnboarding) {
    val acento = corAcento(pagina.acento)

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Brilho radial no canto superior direito, na cor do acento.
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(acento.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.05f),
                        radius = size.width * 0.9f,
                    ),
                )
            }
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LogoElo()

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = pagina.rotuloTopo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = pagina.titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = if (pagina.tituloComAcento) acento else MaterialTheme.colorScheme.onBackground,
            )
        }

        Text(
            text = pagina.descricao,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            pagina.itens.forEach { item ->
                ItemDestaqueRow(item, acento)
            }
        }
    }
}

@Composable
private fun ItemDestaqueRow(item: ItemDestaque, acento: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(acento.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icone, null, tint = acento, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = item.texto,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun LogoElo() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(EloTintaEscura),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Logo Elo",
            modifier = Modifier.size(84.dp),
        )
    }
}

/**
 * Resolve a cor do acento. Para o acento neutro usa [onBackground] (adapta ao
 * tema); coral e teal vêm das cores de marca já resolvidas para claro/escuro.
 */
@Composable
private fun corAcento(acento: Acento): Color = when (acento) {
    Acento.Neutro -> MaterialTheme.colorScheme.onBackground
    Acento.Cliente -> EloTheme.colors.coral
    Acento.Profissional -> EloTheme.colors.teal
}