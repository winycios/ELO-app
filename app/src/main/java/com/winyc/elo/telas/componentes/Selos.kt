package com.winyc.elo.telas.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.winyc.elo.backend.model.search.CriterioSelo
import com.winyc.elo.backend.model.search.DimensaoSelo
import com.winyc.elo.backend.model.search.SeloProfissional
import com.winyc.elo.backend.model.search.SeloRecomendacao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal val VerdeDestaque = Color(0xFF12A15A)
internal val AzulDestaque = Color(0xFF2F6BFF)
internal val RoxoDestaque = Color(0xFF8B5CF6)

@Composable
@ReadOnlyComposable
internal fun corDoSelo(selo: SeloRecomendacao): Color = when (selo) {
    SeloRecomendacao.MELHOR_ESCOLHA -> MaterialTheme.colorScheme.primary
    SeloRecomendacao.REQUISITADO -> RoxoDestaque
    SeloRecomendacao.PERTO_E_POPULAR -> AzulDestaque
    SeloRecomendacao.TALENTO_DA_REGIAO -> VerdeDestaque
}

internal fun iconeDoSelo(selo: SeloRecomendacao): ImageVector = when (selo) {
    SeloRecomendacao.MELHOR_ESCOLHA -> Icons.Outlined.WorkspacePremium
    SeloRecomendacao.REQUISITADO -> Icons.AutoMirrored.Filled.TrendingUp
    SeloRecomendacao.PERTO_E_POPULAR -> Icons.Outlined.NearMe
    SeloRecomendacao.TALENTO_DA_REGIAO -> Icons.Outlined.NewReleases
}


internal fun textoDoSelo(selo: SeloProfissional): String {
    val expressoes = selo.aspectos.map { expressaoElogio(it) }
    return when (expressoes.size) {
        0 -> selo.selo.justificativa
        1 -> "Muito elogiado ${expressoes[0]}."
        else -> "Muito elogiado ${expressoes[0]} e ${expressoes[1]}."
    }
}

@Composable
internal fun Pill(
    texto: String,
    fundo: Color,
    corTexto: Color,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icone != null) {
            Icon(icone, null, tint = corTexto, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelSmall, color = corTexto, maxLines = 1)
    }
}

@Composable
internal fun SeloFaixaFoto(selo: SeloRecomendacao, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(corDoSelo(selo).copy(alpha = 0.94f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(iconeDoSelo(selo), null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            selo.rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
internal fun SeloChip(selo: SeloRecomendacao, modifier: Modifier = Modifier) {
    val cor = corDoSelo(selo)
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(cor.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(iconeDoSelo(selo), null, tint = cor, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(5.dp))
        Text(
            selo.rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = cor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
internal fun MotivoSelo(
    selo: SeloProfissional?,
    modifier: Modifier = Modifier,
    linhas: Int = 2,
) {
    if (selo == null) {
        Text(
            "",
            style = MaterialTheme.typography.labelSmall,
            minLines = linhas,
            maxLines = linhas,
            modifier = modifier,
        )
        return
    }
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Icon(
            iconeDoSelo(selo.selo),
            null,
            tint = corDoSelo(selo.selo),
            modifier = Modifier
                .padding(top = 1.dp)
                .size(12.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            textoDoSelo(selo),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = linhas,
            maxLines = linhas,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CardSelo(
    selo: SeloProfissional,
    criterios: List<CriterioSelo>,
    onComoFunciona: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cor = corDoSelo(selo.selo)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onComoFunciona),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(cor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(iconeDoSelo(selo.selo), null, tint = cor, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    selo.selo.rotulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = cor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Outlined.Info,
                    "Como funciona este selo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }

            Text(
                textoDoSelo(selo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (criterios.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    criterios.forEach { criterio ->
                        ChipCriterio(texto = resumoDoCriterio(criterio), cor = cor)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipCriterio(texto: String, cor: Color) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(cor.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Check, null, tint = cor, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(5.dp))
        Text(texto, style = MaterialTheme.typography.labelSmall, color = cor, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SeloSheet(
    selo: SeloProfissional,
    criterios: List<CriterioSelo>,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val cor = corDoSelo(selo.selo)

    ModalBottomSheet(onDismissRequest = onFechar, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(iconeDoSelo(selo.selo), null, tint = cor, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        "Selo ${selo.selo.rotulo}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Por que este profissional recebeu",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    "Recolher",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { recolherSheet(scope, sheetState, onFechar) }
                        .padding(4.dp),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Text(
                selo.selo.explicacao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (criterios.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TituloBloco("No caso deste profissional")
                    criterios.forEach { criterio ->
                        LinhaCriterio(
                            icone = iconeDoCriterio(criterio),
                            texto = detalheDoCriterio(criterio),
                            cor = cor,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TituloBloco("Como o Elo calcula")
                Text(
                    "Comparamos os profissionais que aparecem na sua busca em três " +
                        "dimensões. O peso de cada uma:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DimensaoSelo.entries.forEach { dimensao -> LinhaDimensao(dimensao, cor) }
            }

            Text(
                "Os selos são calculados automaticamente a cada busca e comparam apenas os " +
                    "profissionais que ela retornou — por isso podem mudar de uma busca para " +
                    "outra. Eles ajudam a comparar, mas não substituem a sua avaliação nem " +
                    "garantem o resultado do serviço.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TituloBloco(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun LinhaCriterio(icone: ImageVector, texto: String, cor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(cor.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, null, tint = cor, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LinhaDimensao(dimensao: DimensaoSelo, cor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                dimensao.rotulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${dimensao.percentual}%",
                style = MaterialTheme.typography.labelLarge,
                color = cor,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(dimensao.peso.toFloat())
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(cor),
            )
        }
        Text(
            dimensao.descricao,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun recolherSheet(
    scope: CoroutineScope,
    sheetState: SheetState,
    aoFim: () -> Unit,
) {
    scope.launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) aoFim()
    }
}

/* ---------------------------- Textos dos critérios ---------------------------- */

private fun iconeDoCriterio(criterio: CriterioSelo): ImageVector = when (criterio) {
    is CriterioSelo.Nota -> Icons.Filled.Star
    is CriterioSelo.Opinioes -> Icons.Outlined.ThumbUp
    is CriterioSelo.Contratacoes -> Icons.Outlined.Handyman
    is CriterioSelo.Proximidade -> Icons.Outlined.NearMe
    is CriterioSelo.Elogios -> Icons.Outlined.AutoAwesome
}

/** Versão curta, para os chips do card. */
private fun resumoDoCriterio(criterio: CriterioSelo): String = when (criterio) {
    is CriterioSelo.Nota -> "Nota ${formatarNota(criterio.valor)}"
    is CriterioSelo.Opinioes -> criterio.percentualPositivo
        ?.let { "${it.toInt()}% positivos" }
        ?: "${criterio.comentarios} comentários lidos"

    is CriterioSelo.Contratacoes -> "${criterio.concluidos} ${servicos(criterio.concluidos)}"
    is CriterioSelo.Proximidade -> formatarDistancia(criterio.km).orEmpty()
    is CriterioSelo.Elogios -> rotuloAspecto(criterio.aspectos.first(), positivo = true)
}

/** Versão explicada, para as linhas da folha. */
private fun detalheDoCriterio(criterio: CriterioSelo): String = when (criterio) {
    is CriterioSelo.Nota ->
        "Nota ${formatarNota(criterio.valor)} em ${criterio.avaliacoes} " +
            "${avaliacoes(criterio.avaliacoes)} de clientes"

    is CriterioSelo.Opinioes -> criterio.percentualPositivo
        ?.let {
            "${it.toInt()}% dos ${criterio.comentarios} comentários analisados são positivos"
        }
        ?: "${criterio.comentarios} comentários analisados pela leitura automática"

    is CriterioSelo.Contratacoes ->
        "${criterio.concluidos} ${servicos(criterio.concluidos)} concluídos pelo Elo"

    is CriterioSelo.Proximidade ->
        "A ${formatarDistancia(criterio.km)} do endereço que você usa no app"

    is CriterioSelo.Elogios -> "Mais elogiado por: " +
        criterio.aspectos.joinToString(" e ") {
            rotuloAspecto(it, positivo = true).lowercase()
        }
}

private fun servicos(quantidade: Int) = if (quantidade == 1) "serviço" else "serviços"

private fun avaliacoes(quantidade: Int) = if (quantidade == 1) "avaliação" else "avaliações"
