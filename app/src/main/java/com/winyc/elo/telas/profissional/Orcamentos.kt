package com.winyc.elo.telas.profissional

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winyc.elo.R
import com.winyc.elo.telas.cliente.AvaliarSheet
import com.winyc.elo.telas.cliente.ContatoSheet

private val Verde = Color(0xFF12A15A)

internal enum class StatusOrcamento { Novo, Aprovado, Historico }

internal data class ItemValor(val descricao: String, val valor: String)

/** Uma solicitação de orçamento vinda de um cliente. */
internal data class Orcamento(
    val cliente: String,
    val avaliacao: Double,
    val tempo: String,
    val servico: String,
    val descricao: String,
    val distancia: String,
    val data: String,
    val periodo: String,
    val telefone: String,
    val status: StatusOrcamento,
    val valor: String,
    val horario: String,
    val endereco: String,
    val dataAgendada: String,
    val itens: List<ItemValor>,
    val observacoes: String,
)

private val ORCAMENTOS = listOf(
    Orcamento(
        cliente = "Mariana Costa", avaliacao = 4.8, tempo = "Há 2h",
        servico = "Instalação de chuveiro elétrico",
        descricao = "Preciso instalar um chuveiro novo de 7500W no banheiro principal. Já tenho o chuveiro comprado.",
        distancia = "2,1 km", data = "22/04/2026", periodo = "Manhã",
        telefone = "(11) 99182-7744", status = StatusOrcamento.Novo, valor = "",
        horario = "Manhã (8h-12h)", endereco = "Rua das Palmeiras, 88 - Perdizes",
        dataAgendada = "22/04/2026 08:00", itens = emptyList(), observacoes = "",
    ),
    Orcamento(
        cliente = "Rafael Souza", avaliacao = 4.5, tempo = "Há 5h",
        servico = "Troca de quadro de disjuntores",
        descricao = "Quadro antigo com disjuntor geral queimando. Casa de 3 quartos, fiação já nova.",
        distancia = "4,8 km", data = "25/04/2026", periodo = "Tarde",
        telefone = "(11) 98771-2210", status = StatusOrcamento.Novo, valor = "",
        horario = "Tarde (13h-18h)", endereco = "Rua Cardoso, 210 - Butantã",
        dataAgendada = "25/04/2026 13:00", itens = emptyList(), observacoes = "",
    ),
    Orcamento(
        cliente = "Bruno Tavares", avaliacao = 4.7, tempo = "Há 1 dia",
        servico = "Instalação elétrica residencial completa",
        descricao = "Casa nova em construção, preciso instalar toda a parte elétrica: quadro, tomadas, interruptores e pontos de luz em 4 cômodos.",
        distancia = "1,7 km", data = "23/04/2026", periodo = "Manhã",
        telefone = "(11) 99640-3301", status = StatusOrcamento.Aprovado, valor = "R$ 850,00",
        horario = "Manhã (8h-12h)", endereco = "Rua Harmonia, 340 - Vila Madalena",
        dataAgendada = "23/04/2026 10:00 - 24/04/2026 12:00",
        itens = listOf(
            ItemValor("Mão de obra (2 dias)", "R$ 500,00"),
            ItemValor("Material elétrico", "R$ 280,00"),
            ItemValor("Disjuntores e quadro", "R$ 40,00"),
            ItemValor("Deslocamento", "R$ 30,00"),
        ),
        observacoes = "Inclui instalação de quadro de distribuição com 8 disjuntores, pontos de tomada 110V/220V em todos os cômodos e pontos de luz com suporte para luminária. Material de primeira linha. Garantia de 90 dias em todo o serviço.",
    ),
    Orcamento(
        cliente = "Eduardo Pinto", avaliacao = 4.2, tempo = "Há 3 dias",
        servico = "Manutenção elétrica geral",
        descricao = "Revisão completa da rede elétrica da casa. Algumas tomadas não funcionam.",
        distancia = "5,5 km", data = "18/04/2026", periodo = "Tarde",
        telefone = "(11) 98123-9080", status = StatusOrcamento.Historico, valor = "R$ 380,00",
        horario = "Tarde (13h-18h)", endereco = "Rua do Sol, 55 - Tatuapé",
        dataAgendada = "18/04/2026 14:00", itens = emptyList(), observacoes = "",
    ),
    Orcamento(
        cliente = "Patricia Alves", avaliacao = 4.7, tempo = "Há 1 semana",
        servico = "Troca de ventilador de teto",
        descricao = "Remover ventilador antigo e instalar um novo, já comprado.",
        distancia = "3,8 km", data = "12/04/2026", periodo = "Tarde",
        telefone = "(11) 99055-6612", status = StatusOrcamento.Historico, valor = "R$ 150,00",
        horario = "Tarde (13h-18h)", endereco = "Rua Azul, 900 - Santana",
        dataAgendada = "12/04/2026 15:00", itens = emptyList(), observacoes = "",
    ),
)

/** Tela "Orçamentos": solicitações de clientes, em abas Novos/Aprovado/Histórico. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrcamentosScreen(modifier: Modifier = Modifier) {
    var aba by rememberSaveable { mutableStateOf(StatusOrcamento.Novo) }

    var contatoDe by remember { mutableStateOf<Orcamento?>(null) }
    var detalhesDe by remember { mutableStateOf<Orcamento?>(null) }
    var enviarPara by remember { mutableStateOf<Orcamento?>(null) }
    var avaliarDe by remember { mutableStateOf<Orcamento?>(null) }

    val visiveis = remember(aba) { ORCAMENTOS.filter { it.status == aba } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.orcamentos),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.solicitacoes_de_clientes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Abas(
                atual = aba,
                onSelecionar = { aba = it },
            )
        }

        items(visiveis) { orc ->
            when (orc.status) {
                StatusOrcamento.Novo -> CardNovo(
                    orc = orc,
                    onAnalisar = { enviarPara = orc },
                    onConversar = { contatoDe = orc },
                )
                StatusOrcamento.Aprovado -> CardAprovado(
                    orc = orc,
                    onDetalhes = { detalhesDe = orc },
                    onChat = { contatoDe = orc },
                )
                StatusOrcamento.Historico -> CardHistorico(
                    orc = orc,
                    onAvaliar = { avaliarDe = orc },
                )
            }
        }
    }

    contatoDe?.let { orc ->
        ContatoSheet(
            nome = orc.cliente,
            subtitulo = "Cliente",
            telefone = orc.telefone,
            onFechar = { contatoDe = null },
        )
    }
    detalhesDe?.let { orc ->
        DetalhesServicoSheet(orc = orc, onFechar = { detalhesDe = null })
    }
    enviarPara?.let { orc ->
        EnviarOrcamentoSheet(orc = orc, onFechar = { enviarPara = null })
    }
    avaliarDe?.let { orc ->
        AvaliarSheet(
            nome = orc.cliente,
            titulo = stringResource(R.string.avaliar_cliente),
            onFechar = { avaliarDe = null },
        )
    }
}

/* ---------------------------- Abas ---------------------------- */

@Composable
private fun Abas(atual: StatusOrcamento, onSelecionar: (StatusOrcamento) -> Unit) {
    val contagens = remember { ORCAMENTOS.groupingBy { it.status }.eachCount() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Aba(stringResource(R.string.aba_novos), contagens[StatusOrcamento.Novo] ?: 0, atual == StatusOrcamento.Novo) {
            onSelecionar(StatusOrcamento.Novo)
        }
        Aba(stringResource(R.string.aba_aprovado), contagens[StatusOrcamento.Aprovado] ?: 0, atual == StatusOrcamento.Aprovado) {
            onSelecionar(StatusOrcamento.Aprovado)
        }
        Aba(stringResource(R.string.aba_historico), contagens[StatusOrcamento.Historico] ?: 0, atual == StatusOrcamento.Historico) {
            onSelecionar(StatusOrcamento.Historico)
        }
    }
}

@Composable
private fun RowScope.Aba(texto: String, contagem: Int, selecionada: Boolean, onClick: () -> Unit) {
    val corTexto = if (selecionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selecionada) MaterialTheme.colorScheme.surface else Color.Transparent)
            .then(
                if (selecionada) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(9.dp))
                else Modifier,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = corTexto,
            fontWeight = if (selecionada) FontWeight.Medium else FontWeight.Normal,
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .widthIn(min = 18.dp)
                .clip(CircleShape)
                .background(if (selecionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f))
                .padding(horizontal = 6.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = contagem.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (selecionada) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/* ---------------------------- Cards ---------------------------- */

@Composable
private fun CardNovo(orc: Orcamento, onAnalisar: () -> Unit, onConversar: () -> Unit) {
    OrcamentoCardBase {
        CabecalhoCliente(orc, badge = { BadgePill("Novo", Verde) })
        Text(orc.servico, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            orc.descricao,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MetaLinha(orc)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.recusar_orcamento))
            }
            Button(onClick = onAnalisar, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.analisar_orcamento))
            }
        }
        BotaoTonal(
            texto = stringResource(R.string.conversar_com_cliente),
            icone = Icons.Outlined.ChatBubbleOutline,
            onClick = onConversar,
        )
    }
}

@Composable
private fun CardAprovado(orc: Orcamento, onDetalhes: () -> Unit, onChat: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        // Faixa verde "aprovado".
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Verde)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Orçamento aprovado pelo cliente!",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            Text(orc.tempo, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCliente(orc.cliente)
                Spacer(Modifier.width(12.dp))
                Column {
                    Rating(orc.cliente, orc.avaliacao)
                    Text(orc.servico, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            CaixaValor("Valor aprovado", orc.valor, Verde)
            MetaLinha(orc)
            BotaoTonal(
                texto = stringResource(R.string.ver_detalhes_do_servico),
                icone = Icons.Outlined.Description,
                onClick = onDetalhes,
            )
            OutlinedButton(onClick = onChat, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.ChatBubbleOutline, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.chat))
            }
        }
    }
}

@Composable
private fun CardHistorico(orc: Orcamento, onAvaliar: () -> Unit) {
    OrcamentoCardBase {
        CabecalhoCliente(orc, badge = { BadgePill("Concluído", Verde) })
        Text(orc.servico, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            orc.descricao,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MetaLinha(orc)
        CaixaValor("Orçamento final", orc.valor, MaterialTheme.colorScheme.onSurface, neutra = true)
        Button(
            onClick = onAvaliar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.StarBorder, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.avaliar_cliente))
        }
    }
}

/* ---------------------------- Peças ---------------------------- */

@Composable
private fun OrcamentoCardBase(conteudo: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = conteudo,
        )
    }
}

@Composable
private fun CabecalhoCliente(orc: Orcamento, badge: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        AvatarCliente(orc.cliente)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Rating(orc.cliente, orc.avaliacao)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Schedule,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(orc.tempo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        badge()
    }
}

@Composable
private fun Rating(nome: String, nota: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(nome, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Filled.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(2.dp))
        Text(nota.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MetaLinha(orc: Orcamento) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MetaItem(Icons.Outlined.LocationOn, orc.distancia)
        MetaItem(Icons.Outlined.CalendarToday, orc.data)
        MetaItem(Icons.Outlined.Schedule, orc.periodo)
    }
}

@Composable
private fun MetaItem(icone: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Caixa de valor: verde para "aprovado", neutra para "orçamento final". */
@Composable
private fun CaixaValor(rotulo: String, valor: String, cor: Color, neutra: Boolean = false) {
    val fundo = if (neutra) MaterialTheme.colorScheme.surfaceVariant else Verde.copy(alpha = 0.12f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(fundo)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            rotulo,
            style = MaterialTheme.typography.bodyMedium,
            color = if (neutra) MaterialTheme.colorScheme.onSurfaceVariant else cor,
            modifier = Modifier.weight(1f),
        )
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            color = cor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun BadgePill(texto: String, cor: Color) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = cor,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(CircleShape)
            .background(cor.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Botão preenchido claro (primaryContainer + texto na cor do contexto). */
@Composable
private fun BotaoTonal(texto: String, icone: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Icon(icone, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto)
    }
}

/** Avatar circular com as iniciais do cliente. */
@Composable
internal fun AvatarCliente(nome: String, tamanho: androidx.compose.ui.unit.Dp = 48.dp) {
    val iniciais = nome.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(CircleShape)
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
