package com.winyc.elo.telas.profissional

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Verde = Color(0xFF12A15A)
private val Azul = Color(0xFF2F6FED)

/** Uma barra do gráfico de ganhos da semana. */
private data class BarraDia(val dia: String, val fracao: Float)

/** Um serviço na lista "mais feitos". */
private data class ServicoFeito(val nome: String, val pedidos: Int)

private val SEMANA = listOf(
    BarraDia("Seg", 0.45f),
    BarraDia("Ter", 0.60f),
    BarraDia("Qua", 0.35f),
    BarraDia("Qui", 0.85f),
    BarraDia("Sex", 0.55f),
    BarraDia("Sab", 0.75f),
    BarraDia("Dom", 0.50f),
)

private val SERVICOS = listOf(
    ServicoFeito("Instalações", 12),
    ServicoFeito("Manutenção", 7),
    ServicoFeito("Reparos", 4),
)

/** Painel Pro: ganhos, avaliações e resumo de desempenho do profissional. */
@Composable
fun PainelScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(
                text = "Olá, Carlos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Painel Pro",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        BannerOrcamentos(quantidade = 2)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                icone = Icons.AutoMirrored.Filled.TrendingUp,
                corIcone = Verde,
                rotulo = "Ganhos do mês",
                valor = "R$ 4.280",
                delta = "18% vs mês passado",
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icone = Icons.Outlined.CheckCircle,
                corIcone = Verde,
                rotulo = "Concluídos",
                valor = "23",
                delta = "12% vs mês passado",
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                icone = Icons.Filled.Star,
                corIcone = MaterialTheme.colorScheme.tertiary,
                rotulo = "Avaliação",
                valor = "4.9",
                unidade = "/5",
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icone = Icons.Outlined.Bolt,
                corIcone = Azul,
                rotulo = "Taxa de resposta",
                valor = "96%",
            )
        }

        GanhosSemanaCard()
        ServicosMaisFeitosCard()
    }
}

@Composable
private fun BannerOrcamentos(quantidade: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Aguardando sua resposta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
                Text(
                    text = "$quantidade novos orçamentos",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun StatCard(
    icone: ImageVector,
    corIcone: Color,
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    unidade: String? = null,
    delta: String? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(corIcone.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = null,
                    tint = corIcone,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = rotulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = valor,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (unidade != null) {
                    Text(
                        text = " $unidade",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
            }
            if (delta != null) {
                Text(
                    text = "↗ $delta",
                    style = MaterialTheme.typography.bodySmall,
                    color = Verde,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun GanhosSemanaCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ganhos da semana",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "↗ +24%",
                    style = MaterialTheme.typography.labelLarge,
                    color = Verde,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = "R$ 2.240",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.size(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                SEMANA.forEach { barra ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(barra.fracao)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = barra.dia,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServicosMaisFeitosCard() {
    val maximo = SERVICOS.maxOf { it.pedidos }.toFloat()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Serviços mais feitos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            SERVICOS.forEach { servico ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(
                            text = servico.nome,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${servico.pedidos} pedidos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BarraProgresso(fracao = servico.pedidos / maximo)
                }
            }
        }
    }
}

@Composable
private fun BarraProgresso(fracao: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fracao)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}
