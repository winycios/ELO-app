package com.winyc.elo.telas.profissional

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winyc.elo.R
import com.winyc.elo.ui.theme.EloTheme

/* ============================ Cores de apoio dos selos ============================ */

private val Verde = Color(0xFF12A15A)
private val Azul = Color(0xFF2F6BFF)
private val Ambar = Color(0xFFDD8A15)

/* ============================ Modelos (mock, compartilhados) ============================ */

/** Dados públicos do profissional — o que o cliente vê e o que o Pro edita. */
internal data class PerfilPublico(
    val nome: String,
    val profissao: String,
    val fotoUrl: String,
    val bio: String,
    val area: String,
    val tags: List<String>,
)

/** Um serviço oferecido pelo profissional. */
internal data class ServicoPro(
    val id: Int,
    val categoria: String,
    val titulo: String,
    val descricao: String,
    val faixaPreco: String,
    val pontos: List<String>,
)

private val PERFIL_INICIAL = PerfilPublico(
    nome = "Carlos Silva",
    profissao = "Eletricista",
    fotoUrl = "https://images.unsplash.com/photo-1621905252507",
    bio = "Eletricista profissional com mais de 15 anos de experiência. " +
        "Especializado em instalações residenciais e comerciais, manutenção preventiva e corretiva.",
    area = "São Paulo - Zona Oeste e Centro (até 10 km)",
    tags = listOf("Pontual", "Organizado", "Excelente trabalho", "Justo no preço"),
)

private val SERVICOS_INICIAIS = listOf(
    ServicoPro(
        1, "Instalações", "Instalação elétrica residencial",
        "Instalação completa de tomadas, interruptores, luminárias e quadros de distribuição.",
        "R$ 150 - R$ 400",
        listOf("Material de qualidade", "Garantia de 90 dias", "Mesmo dia"),
    ),
    ServicoPro(
        2, "Instalações", "Manutenção e reparo elétrico",
        "Diagnóstico e conserto de curto-circuito, disjuntores queimados, fiação antiga.",
        "R$ 100 - R$ 250",
        listOf("Atendimento emergencial", "Orçamento transparente"),
    ),
    ServicoPro(
        3, "Instalações", "Instalação de chuveiro",
        "Troca e instalação de chuveiros elétricos 110V/220V com dimensionamento correto.",
        "R$ 120 - R$ 180",
        listOf("Disjuntor dedicado", "Segurança garantida"),
    ),
)

/* ============================ Tela ============================ */

private enum class SheetPro { EditarPerfil, MeusServicos }

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PerfilProScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val saiuLabel = stringResource(R.string.pro_saiu)

    var perfil by remember { mutableStateOf(PERFIL_INICIAL) }
    var disponivel by rememberSaveable { mutableStateOf(true) }
    val servicos = remember { mutableStateListOf<ServicoPro>().apply { addAll(SERVICOS_INICIAIS) } }
    var sheet by remember { mutableStateOf<SheetPro?>(null) }

    val categorias = servicos.map { it.categoria }.distinct().size

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            CabecalhoPro(perfil = perfil, onEditar = { sheet = SheetPro.EditarPerfil })
        }
        item { CardEstatisticas() }
        item { CardDisponibilidade(disponivel = disponivel, onMudar = { disponivel = it }) }
        item {
            CardAcao(
                icone = Icons.Outlined.WorkOutline,
                titulo = stringResource(R.string.pro_meus_servicos),
                subtitulo = stringResource(R.string.pro_contagem_servicos, categorias, servicos.size),
                onClick = { sheet = SheetPro.MeusServicos },
            )
        }
        item { LinhaSelos() }
        item { CardAreaAtendimento(area = perfil.area) }
        item {
            CardAcao(
                icone = Icons.AutoMirrored.Outlined.HelpOutline,
                titulo = stringResource(R.string.pro_ajuda),
                subtitulo = stringResource(R.string.pro_ajuda_sub),
                onClick = { },
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        Toast.makeText(context, saiuLabel, Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.perfil_sair),
                    style = MaterialTheme.typography.titleSmall,
                    color = EloTheme.colors.coral,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Outlined.Logout,
                    null,
                    tint = EloTheme.colors.coral,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }

    when (sheet) {
        SheetPro.EditarPerfil -> EditarPerfilPublicoSheet(
            perfil = perfil,
            onSalvar = { perfil = it },
            onFechar = { sheet = null },
        )

        SheetPro.MeusServicos -> MeusServicosSheet(
            servicos = servicos,
            onFechar = { sheet = null },
        )

        null -> Unit
    }
}

@Composable
private fun CabecalhoPro(perfil: PerfilPublico, onEditar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCliente(perfil.nome, tamanho = 64.dp)
                Spacer(Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            perfil.nome,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Verified,
                            stringResource(R.string.pro_badge_verificado),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        perfil.profissao,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "4.9 (247 ${stringResource(R.string.avaliacoes)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = onEditar)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.pro_editar_perfil_publico),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.Edit,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CardEstatisticas() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 16.dp),
        ) {
            EstatItem("512", stringResource(R.string.pro_stat_servicos), Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline)
            EstatItem("4.9", stringResource(R.string.pro_stat_avaliacao), Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline)
            EstatItem("96%", stringResource(R.string.pro_stat_resposta), Modifier.weight(1f))
        }
    }
}

@Composable
private fun EstatItem(valor: String, rotulo: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(valor, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(rotulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CardDisponibilidade(disponivel: Boolean, onMudar: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.pro_disponivel_titulo),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(
                        if (disponivel) R.string.pro_disponivel_sub_on else R.string.pro_disponivel_sub_off
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = disponivel, onCheckedChange = onMudar)
        }
    }
}

@Composable
private fun CardAcao(icone: ImageVector, titulo: String, subtitulo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LinhaSelos() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Selo(Icons.Filled.Verified, Verde, stringResource(R.string.pro_badge_verificado), Modifier.weight(1f))
        Selo(Icons.Outlined.EmojiEvents, Ambar, stringResource(R.string.pro_badge_top), Modifier.weight(1f))
        Selo(Icons.Outlined.Schedule, Azul, stringResource(R.string.pro_badge_pontual), Modifier.weight(1f))
    }
}

@Composable
private fun Selo(icone: ImageVector, cor: Color, texto: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(cor.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icone, null, tint = cor, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.labelMedium, color = cor, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CardAreaAtendimento(area: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.pro_area_atendimento),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                area,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
