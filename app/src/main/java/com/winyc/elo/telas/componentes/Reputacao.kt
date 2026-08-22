package com.winyc.elo.telas.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
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
import androidx.compose.ui.unit.dp
import com.winyc.elo.backend.model.estimativa.ReputacaoRS

private const val MIN_COMENTARIOS_REPUTACAO = 3

private val VerdeReputacao = Color(0xFF12A15A)
private val AmbarReputacao = Color(0xFFF2A93B)

internal data class ReputacaoUi(
    val comentariosProcessados: Int,
    val resumo: String?,
    val pontosFortes: List<String>,
    val pontosFracos: List<String>,
    val percentualPositivo: Double?,
    val percentualNeutro: Double?,
    val percentualNegativo: Double?,
)

internal fun ReputacaoRS.paraExibicao(): ReputacaoUi? {
    val processados = comentariosProcessados ?: 0
    if (processados < MIN_COMENTARIOS_REPUTACAO) return null

    val fortes = pontosFortes.orEmpty().filter { it.isNotBlank() }
    val fracos = pontosFracos.orEmpty().filter { it.isNotBlank() }
    val resumoLimpo = resumo?.trim()?.takeIf { it.isNotEmpty() }
    if (resumoLimpo == null && fortes.isEmpty() && fracos.isEmpty()) return null

    return ReputacaoUi(
        comentariosProcessados = processados,
        resumo = resumoLimpo,
        pontosFortes = fortes,
        pontosFracos = fracos,
        percentualPositivo = percentualPositivo,
        percentualNeutro = percentualNeutro,
        percentualNegativo = percentualNegativo,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BlocoReputacao(reputacao: ReputacaoUi, modifier: Modifier = Modifier) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Resumo das opiniões",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Gerado por IA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            reputacao.resumo?.let { resumo ->
                Text(
                    resumo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (reputacao.pontosFortes.isNotEmpty() || reputacao.pontosFracos.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    reputacao.pontosFortes.forEach {
                        ChipAspecto(texto = rotuloAspecto(it, positivo = true), positivo = true)
                    }
                    reputacao.pontosFracos.forEach {
                        ChipAspecto(texto = rotuloAspecto(it, positivo = false), positivo = false)
                    }
                }
            }

            BarraSentimento(
                positivo = reputacao.percentualPositivo,
                neutro = reputacao.percentualNeutro,
                negativo = reputacao.percentualNegativo,
            )

            Text(
                "Análise automática dos comentários de ${reputacao.comentariosProcessados} " +
                    (if (reputacao.comentariosProcessados == 1) "cliente" else "clientes") +
                    ". Pode conter imprecisões.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BarraSentimento(positivo: Double?, neutro: Double?, negativo: Double?) {
    val pos = (positivo ?: 0.0).coerceIn(0.0, 100.0).toFloat()
    val neu = (neutro ?: 0.0).coerceIn(0.0, 100.0).toFloat()
    val neg = (negativo ?: 0.0).coerceIn(0.0, 100.0).toFloat()
    val total = pos + neu + neg
    if (total <= 0f) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            listOf(pos to VerdeReputacao, neu to AmbarReputacao, neg to MaterialTheme.colorScheme.error)
                .filter { (valor, _) -> valor > 0f }
                .forEach { (valor, cor) ->
                    Box(
                        modifier = Modifier
                            .weight(valor / total)
                            .height(8.dp)
                            .background(cor),
                    )
                }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendaSentimento("${pos.toInt()}% positivos", VerdeReputacao)
            if (neu > 0f) LegendaSentimento("${neu.toInt()}% neutros", AmbarReputacao)
            if (neg > 0f) LegendaSentimento("${neg.toInt()}% negativos", MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun LegendaSentimento(texto: String, cor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(cor),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            texto,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChipAspecto(texto: String, positivo: Boolean) {
    val cor = if (positivo) VerdeReputacao else AmbarReputacao
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(cor.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (positivo) Icons.Outlined.ThumbUp else Icons.Outlined.ThumbDown,
            null,
            tint = cor,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

internal fun rotuloAspecto(codigo: String, positivo: Boolean): String {
    val chave = codigo.trim().uppercase()
    val rotulos = if (positivo) ASPECTOS_POSITIVOS else ASPECTOS_NEGATIVOS
    return rotulos[chave] ?: chave.lowercase()
        .replace('_', ' ')
        .replaceFirstChar { it.uppercaseChar() }
}

private val ASPECTOS_POSITIVOS = mapOf(
    "PONTUALIDADE" to "Pontualidade",
    "QUALIDADE" to "Qualidade do serviço",
    "RESOLUCAO" to "Resolveu o problema",
    "RAPIDEZ" to "Rapidez",
    "ATENDIMENTO" to "Bom atendimento",
    "ORGANIZACAO" to "Organização",
    "PRECO" to "Preço justo",
    "COMUNICACAO" to "Boa comunicação",
    "CONFIABILIDADE" to "Confiança",
    "SEGURANCA" to "Cuidado no serviço",
    "EXPERIENCIA" to "Domínio técnico",
    "MATERIAIS" to "Bons materiais",
    "DISPONIBILIDADE" to "Agenda disponível",
    "GARANTIA" to "Suporte após o serviço",
    "TRANSPARENCIA" to "Orçamento transparente",
    "CUMPRIMENTO" to "Cumpre o combinado",
    "APRESENTACAO" to "Postura profissional",
    "RECOMENDACAO" to "Clientes recomendam",
)

private val ASPECTOS_NEGATIVOS = mapOf(
    "PONTUALIDADE" to "Relatos de atraso",
    "QUALIDADE" to "Ressalvas na qualidade",
    "RESOLUCAO" to "Problema não resolvido",
    "RAPIDEZ" to "Relatos de demora",
    "ATENDIMENTO" to "Ressalvas no atendimento",
    "ORGANIZACAO" to "Relatos de desorganização",
    "PRECO" to "Ressalvas no preço",
    "COMUNICACAO" to "Falhas de comunicação",
    "CONFIABILIDADE" to "Ressalvas na confiança",
    "SEGURANCA" to "Falta de cuidado",
    "EXPERIENCIA" to "Ressalvas no preparo técnico",
    "MATERIAIS" to "Ressalvas nos materiais",
    "DISPONIBILIDADE" to "Agendamento difícil",
    "GARANTIA" to "Falhas no pós-serviço",
    "TRANSPARENCIA" to "Ressalvas na transparência",
    "CUMPRIMENTO" to "Fora do combinado",
    "APRESENTACAO" to "Ressalvas na postura",
    "RECOMENDACAO" to "Clientes não recomendam",
)

internal fun expressaoElogio(codigo: String): String {
    val chave = codigo.trim().uppercase()
    return EXPRESSOES_ELOGIO[chave] ?: "pelo aspecto ${chave.lowercase().replace('_', ' ')}"
}

private val EXPRESSOES_ELOGIO = mapOf(
    "PONTUALIDADE" to "pela pontualidade",
    "QUALIDADE" to "pela qualidade do serviço",
    "RESOLUCAO" to "por resolver o problema",
    "RAPIDEZ" to "pela rapidez",
    "ATENDIMENTO" to "pelo atendimento",
    "ORGANIZACAO" to "pela organização",
    "PRECO" to "pelo preço justo",
    "COMUNICACAO" to "pela comunicação",
    "CONFIABILIDADE" to "pela confiança que transmite",
    "SEGURANCA" to "pelo cuidado durante o serviço",
    "EXPERIENCIA" to "pelo domínio técnico",
    "MATERIAIS" to "pelos materiais e equipamentos",
    "DISPONIBILIDADE" to "pela disponibilidade de agenda",
    "GARANTIA" to "pelo suporte após o serviço",
    "TRANSPARENCIA" to "pela transparência no orçamento",
    "CUMPRIMENTO" to "por cumprir o combinado",
    "APRESENTACAO" to "pela postura profissional",
    "RECOMENDACAO" to "pela recomendação de outros clientes",
)
