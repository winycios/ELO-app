package com.winyc.elo.telas.profissional

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TaskAlt
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.winyc.elo.R
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemProfissionalRS
import com.winyc.elo.backend.viewModel.FiltroOrcamentoPro
import com.winyc.elo.backend.viewModel.OrcamentosProViewModel
import com.winyc.elo.backend.viewModel.VisaoOrcamentoPro
import com.winyc.elo.telas.cliente.ChipFiltroOrcamento
import com.winyc.elo.telas.cliente.RodapeOrcamentosCarregando
import com.winyc.elo.telas.cliente.RodapeOrcamentosErro
import com.winyc.elo.telas.cliente.RodapeOrcamentosVazio
import com.winyc.elo.telas.cliente.StatusBadge
import com.winyc.elo.telas.cliente.StatusOrcamento
import com.winyc.elo.telas.componentes.AvatarPerfil
import com.winyc.elo.telas.componentes.DURACAO_AVISO_MS
import com.winyc.elo.telas.componentes.TipoAviso
import com.winyc.elo.telas.componentes.ToastAviso
import com.winyc.elo.telas.componentes.dataComFaixaDeHorario
import com.winyc.elo.telas.componentes.formatarBRL
import com.winyc.elo.telas.componentes.formatarDataHora
import com.winyc.elo.telas.componentes.formatarDistancia
import com.winyc.elo.telas.componentes.formatarNota
import com.winyc.elo.telas.componentes.tempoRelativo
import kotlinx.coroutines.delay

private val Verde = Color(0xFF12A15A)

private const val GATILHO_PROXIMA_PAGINA = 5

private const val ITENS_FIXOS_TOPO = 2

private val ROTULOS_FILTRO: Map<FiltroOrcamentoPro, Pair<Int, Int>> = mapOf(
    FiltroOrcamentoPro.Novo to (R.string.aba_novos to R.string.orcamentos_pro_vazio_novos),
    FiltroOrcamentoPro.Enviado to (R.string.aba_enviado to R.string.orcamentos_pro_vazio_enviados),
    FiltroOrcamentoPro.Aprovado to (R.string.aba_aprovado to R.string.orcamentos_pro_vazio_aprovados),
    FiltroOrcamentoPro.Historico to (R.string.aba_historico to R.string.orcamentos_pro_vazio_historico),
)

@StringRes
private fun rotuloDoFiltro(filtro: FiltroOrcamentoPro): Int =
    ROTULOS_FILTRO.getValue(filtro).first

@StringRes
private fun vazioDoFiltro(filtro: FiltroOrcamentoPro): Int =
    ROTULOS_FILTRO.getValue(filtro).second

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrcamentosScreen(
    modifier: Modifier = Modifier,
    vm: OrcamentosProViewModel = viewModel(),
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val detalhe by vm.detalhe.collectAsStateWithLifecycle()
    val horarios by vm.horarios.collectAsStateWithLifecycle()
    val mensagem by vm.mensagem.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.abrirTela() }
    LaunchedEffect(mensagem) {
        if (mensagem != null) {
            delay(DURACAO_AVISO_MS)
            vm.limparMensagem()
        }
    }

    val listState = rememberLazyListState()
    val quantidade = estado.orcamentos.size
    val precisaCarregarMais by remember(quantidade) {
        derivedStateOf {
            if (quantidade == 0) return@derivedStateOf false
            val ultimoVisivel = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
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
                FiltroChips(
                    selecionado = estado.filtro,
                    onSelecionar = vm::selecionarFiltro,
                )
            }

            items(estado.orcamentos, key = { it.id }) { orcamento ->
                CardOrcamento(
                    orcamento = orcamento,
                    onAbrir = { visao -> vm.abrirDetalhe(orcamento.id, visao) },
                )
            }

            item {
                when {
                    estado.carregandoInicial || estado.carregandoMais -> RodapeOrcamentosCarregando()
                    estado.erro != null -> RodapeOrcamentosErro(
                        mensagem = estado.erro!!,
                        onTentarNovamente = vm::carregarInicial,
                    )

                    estado.orcamentos.isEmpty() ->
                        RodapeOrcamentosVazio(stringResource(vazioDoFiltro(estado.filtro)))
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
        DetalheOrcamentoProSheet(
            estado = estadoDetalhe,
            horarios = horarios,
            onFechar = vm::fecharDetalhe,
            onTentarNovamente = vm::tentarNovamenteDetalhe,
            onSemanaAnterior = vm::semanaAnterior,
            onProximaSemana = vm::proximaSemana,
            onTentarNovamenteHorarios = vm::tentarNovamenteHorarios,
            onEnviarOrcamento = vm::enviarOrcamentoFinal,
            onCancelar = vm::cancelarOrcamento,
            onConcluir = vm::concluirOrcamento,
            onAvaliar = vm::avaliarCliente,
        )
    }
}

/* ---------------------------- Filtros ---------------------------- */

@Composable
private fun FiltroChips(
    selecionado: FiltroOrcamentoPro,
    onSelecionar: (FiltroOrcamentoPro) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(FiltroOrcamentoPro.entries, key = { it.api }) { filtro ->
            ChipFiltroOrcamento(
                rotulo = stringResource(rotuloDoFiltro(filtro)),
                ativo = filtro == selecionado,
                onClick = { onSelecionar(filtro) },
            )
        }
    }
}


@Composable
private fun CardOrcamento(
    orcamento: OrcamentoListagemProfissionalRS,
    onAbrir: (VisaoOrcamentoPro) -> Unit,
) {
    val status = StatusOrcamento.de(orcamento.status)
    val aprovado = status == StatusOrcamento.Aprovado

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (aprovado) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        if (aprovado) FaixaAprovado()

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CabecalhoCliente(orcamento, status, mostrarBadge = !aprovado)

            orcamento.descricao?.takeIf { it.isNotBlank() }?.let { descricao ->
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            MetaLinha(orcamento)
            CaixaValor(orcamento, status)
            Acoes(status, orcamento.avaliado == true, onAbrir)
        }
    }
}

@Composable
private fun FaixaAprovado() {
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
            text = stringResource(R.string.orcamento_aprovado_pelo_cliente),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CabecalhoCliente(
    orcamento: OrcamentoListagemProfissionalRS,
    status: StatusOrcamento?,
    mostrarBadge: Boolean,
) {
    val nome = orcamento.nomeUsuario.orEmpty()
    Row(verticalAlignment = Alignment.Top) {
        AvatarPerfil(
            nome = nome,
            fotoUrl = orcamento.fotoUsuario,
            tamanho = 48.dp,
            fonte = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nome,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                orcamento.avaliacaoUsuario?.let { nota ->
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.Star,
                        null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = formatarNota(nota),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            orcamento.categoria?.takeIf { it.isNotBlank() }?.let { categoria ->
                MetaItem(Icons.Outlined.LocalOffer, categoria)
            }
            tempoRelativo(orcamento.dataHoraCriacao)?.let { tempo ->
                MetaItem(Icons.Outlined.Schedule, tempo)
            }
        }
        if (mostrarBadge) StatusBadge(status, orcamento.status)
    }
}

@Composable
private fun MetaLinha(orcamento: OrcamentoListagemProfissionalRS) {
    val distancia = formatarDistancia(orcamento.distanciaKm)
    val quando = dataComFaixaDeHorario(orcamento.inicioProposto, orcamento.fimProposto) ?: formatarDataHora(orcamento.horarioPreferido)
    if (distancia == null && quando == null) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        distancia?.let { MetaItem(Icons.Outlined.NearMe, it) }
        quando?.let { MetaItem(Icons.Outlined.CalendarToday, it) }
    }
}

@Composable
private fun MetaItem(icone: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icone,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CaixaValor(orcamento: OrcamentoListagemProfissionalRS, status: StatusOrcamento?) {
    val valor = formatarBRL(orcamento.valorTotal) ?: return
    val destaque = status == StatusOrcamento.Aprovado
    val rotulo = when (status) {
        StatusOrcamento.Aprovado -> R.string.valor_aprovado
        StatusOrcamento.OrcamentoFinal -> R.string.valor_enviado
        else -> R.string.valor_final
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (destaque) Verde.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(rotulo),
            style = MaterialTheme.typography.bodyMedium,
            color = if (destaque) Verde else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            color = if (destaque) Verde else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Acoes(
    status: StatusOrcamento?,
    avaliado: Boolean,
    onAbrir: (VisaoOrcamentoPro) -> Unit,
) {
    when (status) {
        StatusOrcamento.Pendente -> {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onAbrir(VisaoOrcamentoPro.Cancelar) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.cancelar_servico))
                }
                Button(
                    onClick = { onAbrir(VisaoOrcamentoPro.Orcar) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.analisar_orcamento))
                }
            }
            BotaoTonal(
                texto = stringResource(R.string.conversar_com_cliente),
                icone = Icons.Outlined.ChatBubbleOutline,
                onClick = { onAbrir(VisaoOrcamentoPro.Contato) },
            )
        }

        StatusOrcamento.OrcamentoFinal -> {
            Text(
                text = stringResource(R.string.aguardando_resposta_cliente),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BotaoTonal(
                texto = stringResource(R.string.ver_orcamento_enviado),
                icone = Icons.Outlined.EditNote,
                onClick = { onAbrir(VisaoOrcamentoPro.OrcamentoFinal) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onAbrir(VisaoOrcamentoPro.Contato) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.contato))
                }
                OutlinedButton(
                    onClick = { onAbrir(VisaoOrcamentoPro.Cancelar) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.cancelar_servico))
                }
            }
        }

        StatusOrcamento.Aprovado -> {
            Button(
                onClick = { onAbrir(VisaoOrcamentoPro.Concluir) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.TaskAlt, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.concluir_servico))
            }
            BotaoTonal(
                texto = stringResource(R.string.ver_detalhes_do_servico),
                icone = Icons.Outlined.Description,
                onClick = { onAbrir(VisaoOrcamentoPro.Detalhes) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onAbrir(VisaoOrcamentoPro.Contato) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.contato))
                }
                OutlinedButton(
                    onClick = { onAbrir(VisaoOrcamentoPro.Cancelar) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.cancelar_servico))
                }
            }
        }

        StatusOrcamento.Concluido -> {
            BotaoTonal(
                texto = stringResource(R.string.ver_detalhes_do_servico),
                icone = Icons.Outlined.Description,
                onClick = { onAbrir(VisaoOrcamentoPro.Detalhes) },
            )
            if (avaliado) {
                Text(
                    text = stringResource(R.string.ja_avaliado),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Button(
                    onClick = { onAbrir(VisaoOrcamentoPro.Avaliar) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.StarBorder, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.avaliar_cliente))
                }
            }
        }

        else -> BotaoTonal(
            texto = stringResource(R.string.ver_detalhes),
            icone = Icons.Outlined.Description,
            onClick = { onAbrir(VisaoOrcamentoPro.Detalhes) },
        )
    }
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
