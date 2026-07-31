package com.winyc.elo.telas.cliente

import androidx.annotation.StringRes
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.ThumbUpOffAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.winyc.elo.R
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemRS
import com.winyc.elo.backend.viewModel.MeusOrcamentosViewModel
import com.winyc.elo.backend.viewModel.VisaoOrcamento
import com.winyc.elo.telas.componentes.AvatarPerfil
import com.winyc.elo.telas.componentes.DURACAO_AVISO_MS
import com.winyc.elo.telas.componentes.TipoAviso
import com.winyc.elo.telas.componentes.ToastAviso
import kotlinx.coroutines.delay

private const val GATILHO_PROXIMA_PAGINA = 5

private const val ITENS_FIXOS_TOPO = 2

internal enum class StatusOrcamento(val api: String, @StringRes val rotuloRes: Int) {
    Pendente("pendente", R.string.status_pendente),
    OrcamentoFinal("orcamento_final", R.string.status_orcamento_final),
    Aprovado("aprovado", R.string.status_aprovado),
    Concluido("concluido", R.string.status_concluido),
    Cancelado("cancelado", R.string.status_cancelado);

    companion object {
        fun de(api: String?): StatusOrcamento? =
            entries.firstOrNull { it.api.equals(api?.trim(), ignoreCase = true) }
    }
}

internal data class StatusVisual(val cor: Color, val icone: ImageVector)

internal fun StatusOrcamento.visual(): StatusVisual = when (this) {
    StatusOrcamento.Pendente -> StatusVisual(Color(0xFFDD8A15), Icons.Outlined.Schedule)
    StatusOrcamento.OrcamentoFinal -> StatusVisual(Color(0xFF8B5CF6), Icons.Outlined.EditNote)
    StatusOrcamento.Aprovado -> StatusVisual(Color(0xFF12A788), Icons.Outlined.ThumbUpOffAlt)
    StatusOrcamento.Concluido -> StatusVisual(Color(0xFF12A15A), Icons.Outlined.CheckCircle)
    StatusOrcamento.Cancelado -> StatusVisual(Color(0xFFF2603E), Icons.Outlined.Cancel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeusOrcamentosScreen(
    logado: Boolean,
    onPrecisaLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
    vm: MeusOrcamentosViewModel = viewModel(),
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val detalhe by vm.detalhe.collectAsStateWithLifecycle()
    val mensagem by vm.mensagem.collectAsStateWithLifecycle()

    LaunchedEffect(logado) { vm.abrirTela(logado) }
    LaunchedEffect(mensagem) {
        if (mensagem != null) {
            delay(DURACAO_AVISO_MS)
            vm.limparMensagem()
        }
    }

    var avaliarDe by remember { mutableStateOf<OrcamentoListagemRS?>(null) }

    val listState = rememberLazyListState()
    val quantidade = estado.orcamentos.size
    val precisaCarregarMais by remember(quantidade) {
        derivedStateOf {
            if (quantidade == 0) return@derivedStateOf false
            val ultimoVisivel = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            // Com a página de 20, dispara ao alcançar o 15º card.
            ultimoVisivel >= ITENS_FIXOS_TOPO + quantidade - 1 - GATILHO_PROXIMA_PAGINA
        }
    }
    LaunchedEffect(precisaCarregarMais, estado.podeCarregarMais) {
        if (precisaCarregarMais && estado.podeCarregarMais) vm.carregarMais()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.meus_orcamentos),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.acompanhe_suas_solicitacoes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                StatusChips(
                    selecionado = estado.statusSelecionado,
                    onSelecionar = vm::selecionarStatus,
                )
            }

            items(estado.orcamentos, key = { it.id }) { orcamento ->
                OrcamentoCard(
                    orcamento = orcamento,
                    onContato = { vm.abrirDetalhe(orcamento.id, VisaoOrcamento.Contato) },
                    onDetalhes = { vm.abrirDetalhe(orcamento.id, VisaoOrcamento.Detalhes) },
                    onRevisar = { vm.abrirDetalhe(orcamento.id, VisaoOrcamento.OrcamentoFinal) },
                    onCancelar = { vm.abrirDetalhe(orcamento.id, VisaoOrcamento.Cancelar) },
                    onAvaliar = { avaliarDe = orcamento },
                )
            }

            item {
                when {
                    !logado -> RodapeDeslogado(onEntrar = onPrecisaLogin)
                    estado.carregandoInicial || estado.carregandoMais -> RodapeOrcamentosCarregando()
                    estado.erro != null -> RodapeOrcamentosErro(
                        mensagem = estado.erro!!,
                        onTentarNovamente = vm::carregarInicial,
                    )

                    estado.orcamentos.isEmpty() -> RodapeOrcamentosVazio(stringResource(R.string.orcamentos_vazio))
                }
            }
        }

        ToastAviso(
            mensagem = mensagem,
            tipo = TipoAviso.Sucesso,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

    detalhe?.let { estadoDetalhe ->
        DetalheOrcamentoSheet(
            estado = estadoDetalhe,
            onFechar = vm::fecharDetalhe,
            onTentarNovamente = vm::tentarNovamenteDetalhe,
            onAceitar = vm::aprovarOrcamentoFinal,
            onRecusar = { vm.trocarVisao(VisaoOrcamento.Cancelar) },
            onCancelar = vm::cancelarOrcamento,
        )
    }

    avaliarDe?.let { orcamento ->
        AvaliarSheet(
            nome = orcamento.nomeProfissional.orEmpty(),
            onFechar = { avaliarDe = null },
        )
    }
}

@Composable
private fun StatusChips(
    selecionado: String?,
    onSelecionar: (String?) -> Unit,
) {
    val abas = listOf<Pair<String?, String>>(null to stringResource(R.string.categoria_todos)) +
            StatusOrcamento.entries.map { it.api to stringResource(it.rotuloRes) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(abas, key = { (api, _) -> api ?: "todos" }) { (api, rotulo) ->
            ChipFiltroOrcamento(
                rotulo = rotulo,
                ativo = api == selecionado,
                onClick = { onSelecionar(api) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChipFiltroOrcamento(rotulo: String, ativo: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = ativo,
        onClick = onClick,
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

@Composable
private fun OrcamentoCard(
    orcamento: OrcamentoListagemRS,
    onContato: () -> Unit,
    onDetalhes: () -> Unit,
    onRevisar: () -> Unit,
    onCancelar: () -> Unit,
    onAvaliar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = StatusOrcamento.de(orcamento.status)

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
            Cabecalho(orcamento, status)
            Text(
                text = orcamento.descricao.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            // O profissional já respondeu com o orçamento final: atalho para revisar.
            if (orcamento.orcamentoFinalId != null && status != StatusOrcamento.Cancelado) {
                BannerOrcamentoFinal(onRevisar)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Acoes(status, onContato, onDetalhes, onCancelar, onAvaliar)
    }
}

@Composable
private fun Cabecalho(orcamento: OrcamentoListagemRS, status: StatusOrcamento?) {
    val nome = orcamento.nomeProfissional.orEmpty()
    Row(verticalAlignment = Alignment.Top) {
        AvatarPerfil(
            nome = nome,
            fotoUrl = orcamento.fotoProfissional,
            tamanho = 52.dp,
            fonte = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            LinhaIconeTexto(
                icone = Icons.Outlined.PersonOutline,
                texto = nome,
                estilo = MaterialTheme.typography.titleSmall,
                corTexto = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.size(2.dp))
            LinhaIconeTexto(
                icone = Icons.Outlined.LocalOffer,
                texto = orcamento.categoria.orEmpty(),
                estilo = MaterialTheme.typography.bodySmall,
                corTexto = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        StatusBadge(status, orcamento.status)
    }
}

@Composable
private fun LinhaIconeTexto(
    icone: ImageVector,
    texto: String,
    estilo: TextStyle,
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
        Text(
            text = texto,
            style = estilo,
            color = corTexto,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun StatusBadge(status: StatusOrcamento?, bruto: String? = null) {
    val visual = status?.visual()
        ?: StatusVisual(MaterialTheme.colorScheme.onSurfaceVariant, Icons.Outlined.Schedule)
    val rotulo = status?.let { stringResource(it.rotuloRes) } ?: rotuloDesconhecido(bruto)
    if (rotulo.isBlank()) return

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(visual.cor.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = visual.icone,
            contentDescription = null,
            tint = visual.cor,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = visual.cor,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun rotuloDesconhecido(bruto: String?): String {
    val texto = bruto?.trim()?.replace('_', ' ') ?: return ""
    return texto.replaceFirstChar { it.uppercaseChar() }
}

@Composable
private fun BannerOrcamentoFinal(onRevisar: () -> Unit) {
    val roxo = StatusOrcamento.OrcamentoFinal.visual().cor
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
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    ),
                ) { append(stringResource(R.string.orcamento_final)) }
                append(" ${stringResource(R.string.orcamento_final_disponivel)}")
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

/** O cliente pode cancelar enquanto o serviço não foi concluído nem cancelado. */
private fun StatusOrcamento?.podeCancelar(): Boolean = this == StatusOrcamento.Pendente ||
    this == StatusOrcamento.OrcamentoFinal ||
    this == StatusOrcamento.Aprovado

@Composable
private fun Acoes(
    status: StatusOrcamento?,
    onContato: () -> Unit,
    onDetalhes: () -> Unit,
    onCancelar: () -> Unit,
    onAvaliar: () -> Unit,
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        when (status) {
            StatusOrcamento.Cancelado -> {
                AcaoBotao(
                    icone = Icons.Outlined.Description,
                    texto = stringResource(R.string.ver_detalhes),
                    cor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onDetalhes,
                )
            }

            StatusOrcamento.Concluido -> {
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
                    icone = Icons.Outlined.Phone,
                    texto = stringResource(R.string.contato),
                    cor = MaterialTheme.colorScheme.primary,
                    onClick = onContato,
                )
                VerticalDivider(color = MaterialTheme.colorScheme.outline)
                AcaoBotao(
                    icone = Icons.Outlined.Description,
                    texto = stringResource(R.string.ver_detalhes),
                    cor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onDetalhes,
                )
                if (status.podeCancelar()) {
                    VerticalDivider(color = MaterialTheme.colorScheme.outline)
                    AcaoBotao(
                        icone = Icons.Outlined.Cancel,
                        texto = stringResource(R.string.cancelar),
                        cor = MaterialTheme.colorScheme.error,
                        onClick = onCancelar,
                    )
                }
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

/* ---------------------------- Rodapés da lista ---------------------------- */

@Composable
internal fun RodapeOrcamentosCarregando() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
    }
}

@Composable
internal fun RodapeOrcamentosErro(mensagem: String, onTentarNovamente: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onTentarNovamente) {
            Text(stringResource(R.string.vitrine_tentar_novamente))
        }
    }
}

@Composable
internal fun RodapeOrcamentosVazio(mensagem: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RodapeDeslogado(onEntrar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.orcamentos_deslogado),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onEntrar) {
            Text(stringResource(R.string.deslogado_entrar))
        }
    }
}
