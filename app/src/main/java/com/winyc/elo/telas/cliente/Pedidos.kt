package com.winyc.elo.telas.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.winyc.elo.R

internal enum class StatusPedido(val rotulo: String) {
    EmAndamento("Em andamento"),
    Concluido("Concluído"),
    Pendente("Pendente"),
    OrcamentoFinal("Orçamento final"),
    Cancelado("Cancelado"),
}

internal data class StatusVisual(val cor: Color, val icone: ImageVector)

internal fun StatusPedido.visual(): StatusVisual = when (this) {
    StatusPedido.EmAndamento -> StatusVisual(Color(0xFF2F6FED), Icons.Outlined.Autorenew)
    StatusPedido.Concluido -> StatusVisual(Color(0xFF12A15A), Icons.Outlined.CheckCircle)
    StatusPedido.Pendente -> StatusVisual(Color(0xFFDD8A15), Icons.Outlined.Schedule)
    StatusPedido.OrcamentoFinal -> StatusVisual(Color(0xFF8B5CF6), Icons.Outlined.EditNote)
    StatusPedido.Cancelado -> StatusVisual(Color(0xFFF2603E), Icons.Outlined.Cancel)
}

internal data class Pedido(
    val profissional: String,
    val categoria: String,
    val servico: String,
    val status: StatusPedido,
    val telefone: String,
    val data: String,
    val horario: String,
    val endereco: String,
    val avaliacao: Double,
    val numAvaliacoes: Int,
    val maoDeObra: String,
    val material: String,
    val deslocamento: String,
    val total: String?,
)

private val PEDIDOS = listOf(
    Pedido(
        profissional = "Ana Oliveira", categoria = "Diarista",
        servico = "Limpeza completa do apartamento", status = StatusPedido.EmAndamento,
        telefone = "(11) 98812-4471", data = "20/04/2026", horario = "09:00",
        endereco = "Rua das Acácias, 45 - Moema", avaliacao = 4.8, numAvaliacoes = 189,
        maoDeObra = "R$ 120,00", material = "R$ 0,00", deslocamento = "R$ 15,00", total = null,
    ),
    Pedido(
        profissional = "Carlos Silva", categoria = "Eletricista",
        servico = "Troca de tomadas e disjuntor", status = StatusPedido.Concluido,
        telefone = "(11) 97345-8821", data = "18/04/2026", horario = "14:00",
        endereco = "Rua das Flores, 123 - Pinheiros", avaliacao = 4.9, numAvaliacoes = 247,
        maoDeObra = "R$ 150,00", material = "R$ 60,00", deslocamento = "R$ 20,00", total = "R$ 230,00",
    ),
    Pedido(
        profissional = "Jose Almeida", categoria = "Jardineiro",
        servico = "Poda e manutencao do jardim", status = StatusPedido.Concluido,
        telefone = "(11) 99123-0055", data = "15/04/2026", horario = "08:30",
        endereco = "Av. dos Ipês, 900 - Jardins", avaliacao = 4.7, numAvaliacoes = 132,
        maoDeObra = "R$ 200,00", material = "R$ 40,00", deslocamento = "R$ 25,00", total = "R$ 265,00",
    ),
    Pedido(
        profissional = "Roberto Mendes", categoria = "Encanador",
        servico = "Reparo no encanamento da cozinha", status = StatusPedido.Pendente,
        telefone = "(11) 98220-7788", data = "22/04/2026", horario = "10:00",
        endereco = "Rua Turim, 78 - Lapa", avaliacao = 4.6, numAvaliacoes = 98,
        maoDeObra = "R$ 140,00", material = "R$ 30,00", deslocamento = "R$ 10,00", total = "R$ 180,00",
    ),
    Pedido(
        profissional = "Carlos Silva", categoria = "Eletricista",
        servico = "Instalacao de 4 tomadas na sala e troca de disjuntor",
        status = StatusPedido.OrcamentoFinal,
        telefone = "(11) 97345-8821", data = "24/04/2026", horario = "14:00",
        endereco = "Rua das Flores, 123 - Pinheiros", avaliacao = 4.9, numAvaliacoes = 247,
        maoDeObra = "R$ 180,00", material = "R$ 80,00", deslocamento = "R$ 20,00", total = "R$ 280,00",
    ),
    Pedido(
        profissional = "Paulo Ferreira", categoria = "Pintor",
        servico = "Pintura da sala de estar", status = StatusPedido.Cancelado,
        telefone = "(11) 98004-1122", data = "10/04/2026", horario = "13:00",
        endereco = "Rua Aurora, 12 - Centro", avaliacao = 4.5, numAvaliacoes = 76,
        maoDeObra = "R$ 300,00", material = "R$ 120,00", deslocamento = "R$ 30,00", total = "R$ 450,00",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosScreen(modifier: Modifier = Modifier) {
    val rotuloTodos = stringResource(R.string.categoria_todos)
    var statusSelecionado by rememberSaveable { mutableStateOf(rotuloTodos) }

    // Modais abertos: cada estado guarda o pedido em foco (null = fechado).
    var contatoDe by remember { mutableStateOf<Pedido?>(null) }
    var detalhesDe by remember { mutableStateOf<Pedido?>(null) }
    var orcamentoDe by remember { mutableStateOf<Pedido?>(null) }
    var avaliarDe by remember { mutableStateOf<Pedido?>(null) }

    val pedidosVisiveis = remember(statusSelecionado) {
        if (statusSelecionado == rotuloTodos) PEDIDOS
        else PEDIDOS.filter { it.status.rotulo == statusSelecionado }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.meus_pedidos),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.acompanhe_seus_servicos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            StatusChips(
                todos = rotuloTodos,
                selecionado = statusSelecionado,
                onSelecionar = { statusSelecionado = it },
            )
        }

        items(pedidosVisiveis) { pedido ->
            PedidoCard(
                pedido = pedido,
                onConversar = { contatoDe = pedido },
                onDetalhes = { detalhesDe = pedido },
                onRevisar = { orcamentoDe = pedido },
                onAvaliar = { avaliarDe = pedido },
            )
        }
    }

    contatoDe?.let { pedido ->
        ContatoSheet(
            nome = pedido.profissional,
            subtitulo = pedido.categoria,
            telefone = pedido.telefone,
            onFechar = { contatoDe = null },
        )
    }
    detalhesDe?.let { pedido ->
        DetalhesPedidoSheet(pedido = pedido, onFechar = { detalhesDe = null })
    }
    orcamentoDe?.let { pedido ->
        OrcamentoFinalSheet(pedido = pedido, onFechar = { orcamentoDe = null })
    }
    avaliarDe?.let { pedido ->
        AvaliarSheet(nome = pedido.profissional, onFechar = { avaliarDe = null })
    }
}

@Composable
private fun StatusChips(
    todos: String,
    selecionado: String,
    onSelecionar: (String) -> Unit,
) {
    val rotulos = listOf(todos) + StatusPedido.entries.map { it.rotulo }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(rotulos) { rotulo ->
            val ativo = rotulo == selecionado
            FilterChip(
                selected = ativo,
                onClick = { onSelecionar(rotulo) },
                label = {
                    Text(
                        text = rotulo,
                        fontWeight = if (ativo) FontWeight.Medium else FontWeight.Normal,
                    )
                },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = ativo,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
private fun PedidoCard(
    pedido: Pedido,
    onConversar: () -> Unit,
    onDetalhes: () -> Unit,
    onRevisar: () -> Unit,
    onAvaliar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Cabecalho(pedido)
            Text(
                text = pedido.servico,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (pedido.status == StatusPedido.OrcamentoFinal) {
                BannerOrcamento(pedido.total ?: "Em analise", onRevisar)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Acoes(pedido, onConversar, onDetalhes, onAvaliar)
    }
}

@Composable
private fun Cabecalho(pedido: Pedido) {
    Row(verticalAlignment = Alignment.Top) {
        AvatarPedido(pedido.profissional)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            LinhaIconeTexto(
                icone = Icons.Outlined.PersonOutline,
                texto = pedido.profissional,
                estilo = MaterialTheme.typography.titleSmall,
                corTexto = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.size(2.dp))
            LinhaIconeTexto(
                icone = Icons.Outlined.LocalOffer,
                texto = pedido.categoria,
                estilo = MaterialTheme.typography.bodySmall,
                corTexto = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        StatusBadge(pedido.status)
    }
}

@Composable
private fun LinhaIconeTexto(
    icone: ImageVector,
    texto: String,
    estilo: androidx.compose.ui.text.TextStyle,
    corTexto: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(text = texto, style = estilo, color = corTexto)
    }
}

/** Selo colorido de status no canto do card. */
@Composable
internal fun StatusBadge(status: StatusPedido) {
    val v = status.visual()
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(v.cor.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = v.icone,
            contentDescription = null,
            tint = v.cor,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = status.rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = v.cor,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** Faixa de orçamento aguardando revisão (só no status Orçamento final). */
@Composable
private fun BannerOrcamento(total: String, onRevisar: () -> Unit) {
    val roxo = StatusPedido.OrcamentoFinal.visual().cor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(roxo.copy(alpha = 0.10f))
            .clickable(onClick = onRevisar)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.EditNote,
            contentDescription = null,
            tint = roxo,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                append("Orçamento de ")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    ),
                ) { append(total) }
                append(" aguardando")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${stringResource(R.string.revisar)} →",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Linha de ações do card, dividida em duas metades (ou uma só, se cancelado). */
@Composable
private fun Acoes(
    pedido: Pedido,
    onConversar: () -> Unit,
    onDetalhes: () -> Unit,
    onAvaliar: () -> Unit,
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        when (pedido.status) {
            StatusPedido.Cancelado -> {
                // Só "Ver detalhes", ocupando a largura toda.
                AcaoBotao(
                    icone = Icons.Outlined.Description,
                    texto = stringResource(R.string.ver_detalhes),
                    cor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onDetalhes,
                )
            }

            StatusPedido.Concluido -> {
                AcaoBotao(
                    icone = Icons.Outlined.StarBorder,
                    texto = stringResource(R.string.avaliar),
                    cor = MaterialTheme.colorScheme.tertiary,
                    onClick = onAvaliar,
                )
                VerticalDivider(color = MaterialTheme.colorScheme.outline)
                AcaoBotao(
                    icone = Icons.Outlined.Description,
                    texto = stringResource(R.string.ver_detalhes),
                    cor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onDetalhes,
                )
            }

            else -> {
                AcaoBotao(
                    icone = Icons.Outlined.ChatBubbleOutline,
                    texto = stringResource(R.string.conversar),
                    cor = MaterialTheme.colorScheme.primary,
                    onClick = onConversar,
                )
                VerticalDivider(color = MaterialTheme.colorScheme.outline)
                AcaoBotao(
                    icone = Icons.Outlined.Description,
                    texto = stringResource(R.string.ver_detalhes),
                    cor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onDetalhes,
                )
            }
        }
    }
}

@Composable
private fun RowScope.AcaoBotao(
    icone: ImageVector,
    texto: String,
    cor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = cor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(text = texto, style = MaterialTheme.typography.labelLarge, color = cor)
    }
}

/** Avatar quadrado arredondado com as iniciais do profissional. */
@Composable
internal fun AvatarPedido(nome: String, tamanho: Dp = 52.dp) {
    val iniciais = nome.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = iniciais,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
