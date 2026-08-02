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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.winyc.elo.R
import com.winyc.elo.backend.model.profissional.AvaliacaoResumoRS
import com.winyc.elo.backend.model.profissional.GanhosSemanaRS
import com.winyc.elo.backend.model.profissional.ProfissionalDashboardRS
import com.winyc.elo.backend.model.profissional.ServicoMaisFeitoRS
import com.winyc.elo.backend.viewModel.PainelUi
import com.winyc.elo.backend.viewModel.PainelViewModel
import com.winyc.elo.telas.componentes.DIAS_DA_SEMANA
import com.winyc.elo.telas.componentes.formatarNota
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

private val Verde = Color(0xFF12A15A)
private val Azul = Color(0xFF2F6FED)
private const val SEM_VALOR = "—"

/** Variação percentual de um indicador contra o período anterior. */
private data class Variacao(val percentual: Double, val comparativo: String)

/** Painel Pro: ganhos, avaliações e resumo de desempenho do profissional. */
@Composable
fun PainelScreen(
    onAbrirAgenda: () -> Unit,
    onAbrirOrcamentos: () -> Unit,
    modifier: Modifier = Modifier,
    vm: PainelViewModel = viewModel(),
) {
    val estado by vm.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.carregar() }

    when {
        estado.dashboard == null && estado.carregando -> CarregandoPainel(modifier)
        estado.dashboard == null -> ErroPainel(
            mensagem = estado.erro ?: "Não foi possível carregar o painel.",
            onTentarNovamente = vm::carregar,
            modifier = modifier,
        )

        else -> ConteudoPainel(
            estado = estado,
            onAbrirAgenda = onAbrirAgenda,
            onAbrirOrcamentos = onAbrirOrcamentos,
            modifier = modifier,
        )
    }
}

@Composable
private fun ConteudoPainel(
    estado: PainelUi,
    onAbrirAgenda: () -> Unit,
    onAbrirOrcamentos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dados = estado.dashboard ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Cabecalho(nome = dados.nome, disponivel = dados.modoProfissionalAtivo)

        // Falhou a atualização, mas os números anteriores seguem na tela.
        estado.erro?.let { AvisoDesatualizado(it) }

        BannerOrcamentos(quantidade = dados.novosOrcamentos, onClick = onAbrirOrcamentos)

        CardAgenda(servicosHoje = dados.servicosHoje.toInt(), onClick = onAbrirAgenda)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                icone = Icons.AutoMirrored.Filled.TrendingUp,
                corIcone = Verde,
                rotulo = "Ganhos do mês",
                valor = formatarValor(dados.ganhosMes?.valor),
                variacao = dados.ganhosMes?.variacaoPercentual?.let {
                    Variacao(it, "vs mês passado")
                },
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icone = Icons.Outlined.CheckCircle,
                corIcone = Verde,
                rotulo = "Concluídos",
                valor = (dados.concluidosMes?.quantidade ?: 0L).toString(),
                variacao = dados.concluidosMes?.variacaoPercentual?.let {
                    Variacao(it, "vs mês passado")
                },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCardAvaliacao(
                modifier = Modifier.weight(1f),
                avaliacao = dados.avaliacao,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icone = Icons.Outlined.Bolt,
                corIcone = Azul,
                rotulo = "Taxa de resposta",
                valor = dados.taxaRespostaPercentual
                    ?.let { "${formatarPercentual(it)}%" }
                    ?: SEM_VALOR,
                apoio = "orçamentos respondidos",
            )
        }

        GanhosSemanaCard(dados.ganhosSemana)
        ServicosMaisFeitosCard(dados.servicosMaisFeitos)
    }
}

/* ---------------------------- Estados de carga ---------------------------- */

@Composable
private fun CarregandoPainel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErroPainel(
    mensagem: String,
    onTentarNovamente: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(12.dp))
        Button(onClick = onTentarNovamente) { Text("Tentar novamente") }
    }
}

@Composable
private fun AvisoDesatualizado(mensagem: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

/* ---------------------------- Cabeçalho ---------------------------- */

@Composable
private fun Cabecalho(nome: String?, disponivel: Boolean?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = primeiroNome(nome)?.let { "Olá, $it" } ?: "Olá!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Painel Pro",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (disponivel != null) {
            val cor = if (disponivel) Verde else MaterialTheme.colorScheme.onSurfaceVariant
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(cor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(cor),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (disponivel) "Disponível" else "Indisponível",
                    style = MaterialTheme.typography.labelMedium,
                    color = cor,
                )
            }
        }
    }
}

/* ---------------------------- Atalhos ---------------------------- */

@Composable
private fun BannerOrcamentos(quantidade: Long, onClick: () -> Unit) {
    val pendente = quantidade > 0
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pendente) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (pendente) 0.dp else 1.dp),
    ) {
        val titulo = if (pendente) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface
        val apoio = if (pendente) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
        else MaterialTheme.colorScheme.onSurfaceVariant
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (pendente) "Aguardando sua resposta" else "Orçamentos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = apoio,
                )
                Text(
                    text = if (pendente) {
                        "$quantidade ${if (quantidade == 1L) "novo orçamento" else "novos orçamentos"}"
                    } else {
                        "Tudo respondido por aqui"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = titulo,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (pendente) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CardAgenda(servicosHoje: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.painel_sua_agenda),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.painel_servicos_hoje,
                        servicosHoje,
                        servicosHoje,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* ---------------------------- Indicadores ---------------------------- */

@Composable
private fun StatCard(
    icone: ImageVector,
    corIcone: Color,
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    unidade: String? = null,
    apoio: String? = null,
    variacao: Variacao? = null,
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
            when {
                variacao != null -> LinhaVariacao(variacao)
                apoio != null -> Text(
                    text = apoio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun StatCardAvaliacao(avaliacao: AvaliacaoResumoRS?, modifier: Modifier = Modifier) {
    val media = avaliacao?.media?.takeIf { (avaliacao.quantidade) > 0 }
    val quantidade = avaliacao?.quantidade ?: 0
    StatCard(
        modifier = modifier,
        icone = Icons.Filled.Star,
        corIcone = MaterialTheme.colorScheme.tertiary,
        rotulo = "Avaliação",
        valor = media?.let { formatarNota(it) } ?: SEM_VALOR,
        unidade = media?.let { "/${avaliacao?.notaMaxima ?: 5}" },
        apoio = if (quantidade > 0) {
            "$quantidade ${if (quantidade == 1) "avaliação" else "avaliações"}"
        } else {
            "sem avaliações ainda"
        },
    )
}

@Composable
private fun LinhaVariacao(variacao: Variacao) {
    val estavel = variacao.percentual == 0.0
    val subindo = variacao.percentual > 0
    val cor = when {
        estavel -> MaterialTheme.colorScheme.onSurfaceVariant
        subindo -> Verde
        else -> MaterialTheme.colorScheme.error
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!estavel) {
            Icon(
                imageVector = if (subindo) Icons.AutoMirrored.Filled.TrendingUp
                else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = cor,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = if (estavel) {
                "estável ${variacao.comparativo}"
            } else {
                "${formatarPercentual(variacao.percentual)}% ${variacao.comparativo}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = cor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/* ---------------------------- Ganhos da semana ---------------------------- */

@Composable
private fun GanhosSemanaCard(ganhos: GanhosSemanaRS?) {
    val dias = ganhos?.dias.orEmpty()
    val maximo = dias.maxOfOrNull { it.valor ?: 0.0 } ?: 0.0
    val hoje = LocalDate.now()

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
                ganhos?.variacaoPercentual?.let {
                    LinhaVariacao(Variacao(it, "vs semana passada"))
                }
            }
            Text(
                text = formatarValor(ganhos?.valor),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (dias.isEmpty()) {
                Spacer(Modifier.size(12.dp))
                Text(
                    text = "Ainda não há ganhos registrados nesta semana.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            Spacer(Modifier.size(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                dias.forEach { dia ->
                    val valor = dia.valor ?: 0.0
                    val fracao = if (maximo > 0) (valor / maximo).toFloat() else 0f
                    val ehHoje = dia.data?.let { runCatching { LocalDate.parse(it) }.getOrNull() } == hoje
                    val cor = when {
                        valor <= 0.0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ehHoje -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(max(fracao, 0.04f))
                                .clip(RoundedCornerShape(6.dp))
                                .background(cor),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = rotuloDia(dia.data, dia.diaSemana),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (ehHoje) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (ehHoje) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

/* ---------------------------- Serviços mais feitos ---------------------------- */

@Composable
private fun ServicosMaisFeitosCard(servicos: List<ServicoMaisFeitoRS>) {
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
            if (servicos.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Insights,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Conclua seus primeiros serviços para ver o ranking aqui.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Column
            }
            val maximo = servicos.maxOf { it.quantidade }.coerceAtLeast(1L)
            servicos.forEach { servico ->
                val fracao = servico.percentualDoMaisFeito
                    ?.let { (it / 100.0).toFloat() }
                    ?: (servico.quantidade.toFloat() / maximo)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = servico.nome.orEmpty().ifBlank { "Serviço" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${servico.quantidade} ${if (servico.quantidade == 1L) "pedido" else "pedidos"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BarraProgresso(fracao = fracao.coerceIn(0f, 1f))
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

/* ---------------------------- Formatação ---------------------------- */

private val LOCALE_BR: Locale = Locale.forLanguageTag("pt-BR")

/** Valores do painel vão sem centavos: "R$ 2.060". */
private fun formatarValor(valor: Double?): String =
    "R$ %,d".format(LOCALE_BR, (valor ?: 0.0).roundToLong())

/** "18", "12,5", "1.773" — sempre em módulo; o sinal vira ícone. */
private fun formatarPercentual(valor: Double): String {
    val modulo = abs(valor)
    return if (modulo % 1.0 == 0.0 || modulo >= 100) "%,.0f".format(LOCALE_BR, modulo)
    else "%,.1f".format(LOCALE_BR, modulo)
}

private fun primeiroNome(nome: String?): String? =
    nome?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }
        ?.replaceFirstChar { it.uppercaseChar() }

/** Usa a data para pegar o dia acentuado; se ela não vier, cai no texto da API. */
private fun rotuloDia(dataIso: String?, diaSemana: String?): String {
    val data = dataIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    if (data != null) return DIAS_DA_SEMANA[data.dayOfWeek.value - 1]
    return diaSemana.orEmpty().replaceFirstChar { it.uppercaseChar() }
}
