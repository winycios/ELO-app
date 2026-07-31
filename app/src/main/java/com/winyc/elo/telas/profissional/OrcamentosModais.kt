package com.winyc.elo.telas.profissional

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.winyc.elo.R
import com.winyc.elo.backend.model.orcamento.CategoriaCusto
import com.winyc.elo.backend.model.orcamento.DiaHorariosRS
import com.winyc.elo.backend.model.orcamento.ItemCusto
import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheProfissionalRS
import com.winyc.elo.backend.model.orcamento.OrcamentoFinalCreateRQ
import com.winyc.elo.backend.viewModel.HorariosUi
import com.winyc.elo.backend.viewModel.OrcamentoProDetalheUi
import com.winyc.elo.backend.viewModel.VisaoOrcamentoPro
import com.winyc.elo.telas.cliente.CabecalhoSheet
import com.winyc.elo.telas.cliente.CaixaCarregandoOrcamento
import com.winyc.elo.telas.cliente.CaixaInfo
import com.winyc.elo.telas.cliente.ConteudoContato
import com.winyc.elo.telas.cliente.ConteudoErro
import com.winyc.elo.telas.cliente.InfoLinha
import com.winyc.elo.telas.cliente.LinhaValor
import com.winyc.elo.telas.cliente.SecaoLabel
import com.winyc.elo.telas.cliente.StatusBadge
import com.winyc.elo.telas.cliente.StatusOrcamento
import com.winyc.elo.telas.cliente.recolherBottomModal
import com.winyc.elo.telas.componentes.AvatarPerfil
import com.winyc.elo.telas.componentes.BlocoCancelamento
import com.winyc.elo.telas.componentes.BlocoConclusao
import com.winyc.elo.telas.componentes.FormularioAvaliacao
import com.winyc.elo.telas.componentes.FormularioCancelamento
import com.winyc.elo.telas.componentes.MOTIVOS_CANCELAMENTO_PROFISSIONAL
import com.winyc.elo.telas.componentes.enderecoCompleto
import com.winyc.elo.telas.componentes.faixaDeHorario
import com.winyc.elo.telas.componentes.formatarBRL
import com.winyc.elo.telas.componentes.formatarData
import com.winyc.elo.telas.componentes.formatarDataHora
import com.winyc.elo.telas.componentes.formatarDistancia
import com.winyc.elo.telas.componentes.formatarHoraSlot
import com.winyc.elo.telas.componentes.formatarNota
import com.winyc.elo.telas.componentes.montarDataHoraIso
import com.winyc.elo.telas.componentes.rotuloTipoServico
import java.time.LocalDate
import java.time.LocalTime

private val Verde = Color(0xFF12A15A)
private val Ambar = Color(0xFFDD8A15)

private const val LIMITE_OBSERVACAO = 200

private const val LIMITE_DESCRICAO_CUSTO = 45

private const val LIMITE_ITENS = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetalheOrcamentoProSheet(
    estado: OrcamentoProDetalheUi,
    horarios: HorariosUi,
    onFechar: () -> Unit,
    onTentarNovamente: () -> Unit,
    onSemanaAnterior: () -> Unit,
    onProximaSemana: () -> Unit,
    onTentarNovamenteHorarios: () -> Unit,
    onEnviarOrcamento: (
        inicioProposto: String,
        fimProposto: String,
        observacao: String,
        custos: List<OrcamentoFinalCreateRQ.CustoRQ>,
    ) -> Unit,
    onCancelar: (motivo: String, descricao: String) -> Unit,
    onConcluir: (observacao: String) -> Unit,
    onAvaliar: (nota: Int, comentario: String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val fechar = { recolherBottomModal(scope, sheetState, onFechar) }
    val detalhe = estado.detalhe

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        sheetGesturesEnabled = estado.visao != VisaoOrcamentoPro.Orcar,
        dragHandle = null
    ) {
        if (detalhe == null || estado.carregando) {
            FolhaSimples {
                CabecalhoSheet(
                    titulo = stringResource(tituloDaVisao(estado.visao)),
                    onFechar = fechar
                )
                if (estado.erro != null && !estado.carregando) {
                    ConteudoErro(mensagem = estado.erro, onTentarNovamente = onTentarNovamente)
                } else {
                    CaixaCarregandoOrcamento()
                }
            }
            return@ModalBottomSheet
        }

        when (estado.visao) {
            VisaoOrcamentoPro.Contato -> FolhaSimples {
                ConteudoContato(
                    nome = detalhe.cliente?.nome.orEmpty(),
                    fotoUrl = detalhe.cliente?.fotoPerfil,
                    subtitulo = stringResource(R.string.cliente),
                    telefone = detalhe.cliente?.contato?.telefone,
                    whatsapp = detalhe.cliente?.contato?.whatsapp,
                    verificado = detalhe.cliente?.habilitado == true,
                    onFechar = fechar,
                )
            }

            VisaoOrcamentoPro.Orcar -> ConteudoEnviarOrcamento(
                estado = estado,
                detalhe = detalhe,
                horarios = horarios,
                onFechar = fechar,
                onSemanaAnterior = onSemanaAnterior,
                onProximaSemana = onProximaSemana,
                onTentarNovamenteHorarios = onTentarNovamenteHorarios,
                onEnviar = onEnviarOrcamento,
            )

            VisaoOrcamentoPro.OrcamentoFinal -> FolhaSimples {
                ConteudoOrcamentoFinal(detalhe = detalhe, onFechar = fechar)
            }

            VisaoOrcamentoPro.Cancelar -> FolhaSimples {
                ConteudoCancelar(estado = estado, onFechar = fechar, onCancelar = onCancelar)
            }

            VisaoOrcamentoPro.Concluir -> FolhaSimples {
                ConteudoConcluir(estado = estado, detalhe = detalhe, onFechar = fechar, onConcluir = onConcluir)
            }

            VisaoOrcamentoPro.Avaliar -> FolhaSimples {
                ConteudoAvaliar(estado = estado, detalhe = detalhe, onFechar = fechar, onAvaliar = onAvaliar)
            }

            VisaoOrcamentoPro.Detalhes -> FolhaSimples {
                ConteudoDetalhes(detalhe = detalhe, onFechar = fechar)
            }
        }
    }
}

private fun tituloDaVisao(visao: VisaoOrcamentoPro): Int = when (visao) {
    VisaoOrcamentoPro.Contato -> R.string.contato
    VisaoOrcamentoPro.Orcar -> R.string.enviar_orcamento_final
    VisaoOrcamentoPro.OrcamentoFinal -> R.string.orcamento_final
    VisaoOrcamentoPro.Cancelar -> R.string.cancelar_servico
    VisaoOrcamentoPro.Concluir -> R.string.concluir_servico
    VisaoOrcamentoPro.Avaliar -> R.string.avaliar_cliente
    VisaoOrcamentoPro.Detalhes -> R.string.detalhes_do_servico
}

@Composable
private fun FolhaSimples(conteudo: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        content = conteudo,
    )
}


@Composable
private fun ConteudoDetalhes(detalhe: OrcamentoDetalheProfissionalRS, onFechar: () -> Unit) {
    val solicitacao = detalhe.solicitacao
    val final = detalhe.orcamentoFinal
    val naoInformado = stringResource(R.string.nao_informado)

    CabecalhoSheet(
        titulo = stringResource(R.string.detalhes_do_servico),
        subtitulo = solicitacao?.categoria,
        onFechar = onFechar,
    )

    Spacer(Modifier.size(16.dp))
    CartaoCliente(detalhe, comStatus = true)

    Spacer(Modifier.size(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.size(16.dp))

    SecaoLabel(stringResource(R.string.informacoes_da_solicitacao))
    Spacer(Modifier.size(12.dp))
    InfoLinha(
        Icons.Outlined.Description,
        stringResource(R.string.descricao),
        solicitacao?.descricao?.takeIf { it.isNotBlank() } ?: naoInformado,
    )
    InfoLinha(
        Icons.Outlined.LocalOffer,
        stringResource(R.string.categoria),
        solicitacao?.categoria?.takeIf { it.isNotBlank() } ?: naoInformado,
    )
    InfoLinha(
        Icons.Outlined.CalendarToday,
        stringResource(R.string.horario_preferido),
        formatarDataHora(solicitacao?.horarioPreferido) ?: naoInformado,
    )
    InfoLinha(
        Icons.Outlined.Handyman,
        stringResource(R.string.tipo_servico),
        rotuloTipoServico(solicitacao?.tipoServico) ?: naoInformado,
    )
    InfoLinha(
        Icons.Outlined.NearMe,
        stringResource(R.string.distancia),
        formatarDistancia(solicitacao?.distanciaKm) ?: naoInformado,
    )

    enderecoCompleto(solicitacao?.endereco)?.let { endereco ->
        Spacer(Modifier.size(10.dp))
        CaixaInfo(
            icone = Icons.Outlined.LocationOn,
            rotulo = stringResource(R.string.endereco),
            valor = endereco,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    detalhe.conclusao?.let {
        Spacer(Modifier.size(16.dp))
        BlocoConclusao(it)
    }
    detalhe.cancelamento?.let {
        Spacer(Modifier.size(16.dp))
        BlocoCancelamento(it)
    }

    GaleriaFotos(solicitacao?.imagens.orEmpty())

    if (final != null) {
        Spacer(Modifier.size(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.size(16.dp))
        BlocoOrcamentoFinal(detalhe)
    }

    Spacer(Modifier.size(20.dp))
    Button(onClick = onFechar, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.fechar))
    }
}

@Composable
private fun ConteudoOrcamentoFinal(detalhe: OrcamentoDetalheProfissionalRS, onFechar: () -> Unit) {
    CabecalhoSheet(
        icone = Icons.Outlined.EditNote,
        titulo = stringResource(R.string.orcamento_final),
        subtitulo = stringResource(
            if (StatusOrcamento.de(detalhe.status) == StatusOrcamento.OrcamentoFinal) {
                R.string.aguardando_resposta_cliente
            } else {
                R.string.detalhes_do_servico
            },
        ),
        onFechar = onFechar,
    )

    Spacer(Modifier.size(16.dp))
    CartaoCliente(detalhe, comStatus = true)

    if (detalhe.orcamentoFinal == null) {
        Spacer(Modifier.size(20.dp))
        Text(
            text = stringResource(R.string.orcamento_final_pendente),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Spacer(Modifier.size(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.size(16.dp))
        BlocoOrcamentoFinal(detalhe)
    }

    Spacer(Modifier.size(20.dp))
    Button(onClick = onFechar, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.fechar))
    }
}

@Composable
private fun BlocoOrcamentoFinal(detalhe: OrcamentoDetalheProfissionalRS) {
    val final = detalhe.orcamentoFinal ?: return
    val solicitacao = detalhe.solicitacao

    SecaoLabel(stringResource(R.string.servico))
    Spacer(Modifier.size(6.dp))
    Text(
        text = solicitacao?.descricao.orEmpty(),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(Modifier.size(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CaixaInfo(
            icone = Icons.Outlined.CalendarToday,
            rotulo = stringResource(R.string.data_proposta),
            valor = formatarData(final.inicioProposto) ?: stringResource(R.string.a_combinar),
            modifier = Modifier.weight(1f),
        )
        CaixaInfo(
            icone = Icons.Outlined.Schedule,
            rotulo = stringResource(R.string.horario),
            valor = faixaDeHorario(final.inicioProposto, final.fimProposto)
                ?: stringResource(R.string.a_combinar),
            modifier = Modifier.weight(1f),
        )
    }

    Spacer(Modifier.size(16.dp))
    SecaoLabel(stringResource(R.string.valor))
    Spacer(Modifier.size(10.dp))
    final.custos.forEach { custo ->
        LinhaValor(
            rotulo = custo.descricao.orEmpty(),
            valor = formatarBRL(custo.valor) ?: stringResource(R.string.a_combinar),
        )
    }
    Spacer(Modifier.size(8.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.size(8.dp))
    LinhaValor(
        rotulo = stringResource(R.string.total),
        valor = formatarBRL(final.valorTotal) ?: stringResource(R.string.a_combinar),
        destaque = true,
    )

    final.observacaoProfissional?.takeIf { it.isNotBlank() }?.let { observacao ->
        Spacer(Modifier.size(16.dp))
        CaixaObservacao(observacao)
    }
}

@Composable
private fun CaixaObservacao(texto: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Ambar.copy(alpha = 0.10f))
            .border(1.dp, Ambar.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Outlined.StickyNote2,
                null,
                tint = Ambar,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.observacao_profissional).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Ambar,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private val CATEGORIAS_CUSTO = listOf(
    CategoriaCusto("Mão de obra", Icons.Outlined.Build),
    CategoriaCusto("Material", Icons.Outlined.Inventory2),
    CategoriaCusto("Deslocamento", Icons.Outlined.NearMe),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConteudoEnviarOrcamento(
    estado: OrcamentoProDetalheUi,
    detalhe: OrcamentoDetalheProfissionalRS,
    horarios: HorariosUi,
    onFechar: () -> Unit,
    onSemanaAnterior: () -> Unit,
    onProximaSemana: () -> Unit,
    onTentarNovamenteHorarios: () -> Unit,
    onEnviar: (String, String, String, List<OrcamentoFinalCreateRQ.CustoRQ>) -> Unit,
) {
    val itens = remember { mutableStateListOf<ItemCusto>() }
    var itemPersonalizado by rememberSaveable { mutableStateOf("") }
    var observacoes by rememberSaveable { mutableStateOf("") }
    var dia by rememberSaveable { mutableStateOf<String?>(null) }
    var inicio by rememberSaveable { mutableStateOf<String?>(null) }
    var fim by rememberSaveable { mutableStateOf<String?>(null) }

    val dias = horarios.semana?.dias.orEmpty()
    LaunchedEffect(horarios.semana) {
        if (dia != null && dias.none { it.data == dia }) {
            dia = null
            inicio = null
            fim = null
        }
    }

    val diaAtual = dias.firstOrNull { it.data == dia }
    val opcoesFim = opcoesDeFim(diaAtual, inicio, horarios.semana?.intervaloMinutos)
    val total = itens.sumOf { paraNumero(it.valor) }
    val custos = itens.mapNotNull { item ->
        val valor = paraNumero(item.valor)
        if (valor <= 0.0 || item.nome.isBlank()) null
        else OrcamentoFinalCreateRQ.CustoRQ(item.nome.trim().take(LIMITE_DESCRICAO_CUSTO), valor)
    }
    val podeEnviar = dia != null && inicio != null && fim != null &&
            custos.isNotEmpty() && !estado.salvando

    Column(
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .padding(vertical = 20.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            CabecalhoSheet(
                icone = Icons.Outlined.EditNote,
                titulo = stringResource(R.string.enviar_orcamento_final),
                subtitulo = stringResource(R.string.detalhe_custos_para_cliente),
                onFechar = onFechar,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.size(2.dp))
            CartaoCliente(detalhe, comStatus = false)

            detalhe.solicitacao?.descricao?.takeIf { it.isNotBlank() }?.let { descricao ->
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            formatarDataHora(detalhe.solicitacao?.horarioPreferido)?.let { preferido ->
                CaixaInfo(
                    icone = Icons.Outlined.Schedule,
                    rotulo = stringResource(R.string.horario_preferido),
                    valor = preferido,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            GaleriaFotos(detalhe.solicitacao?.imagens.orEmpty())

            SecaoLabel(stringResource(R.string.escolha_data_do_servico))
            CalendarioServico(
                horarios = horarios,
                dias = dias,
                diaAtual = diaAtual,
                diaSelecionado = dia,
                inicioSelecionado = inicio,
                fimSelecionado = fim,
                opcoesFim = opcoesFim,
                onSelecionarDia = { dia = it; inicio = null; fim = null },
                onSelecionarInicio = { inicio = it; fim = null },
                onSelecionarFim = { fim = it },
                onSemanaAnterior = onSemanaAnterior,
                onProximaSemana = onProximaSemana,
                onTentarNovamente = onTentarNovamenteHorarios,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                SecaoLabelComIcone(
                    texto = stringResource(R.string.detalhamento_de_custos),
                    icone = Icons.Outlined.Paid,
                )
                if (itens.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.itens_contagem, itens.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            Text(
                text = stringResource(R.string.adicionar_categoria),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CATEGORIAS_CUSTO.forEach { categoria ->
                    ChipCategoria(
                        icone = categoria.icone,
                        texto = categoria.nome,
                        ativa = itens.any { it.nome == categoria.nome },
                        onClick = {
                            if (itens.size < LIMITE_ITENS) {
                                itens.add(ItemCusto(categoria.nome, categoria.icone))
                            }
                        },
                    )
                }
            }

            if (itens.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                ) {
                    itens.forEachIndexed { indice, item ->
                        if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        ItemCustoRow(
                            item = item,
                            onValor = { novo -> itens[indice] = item.copy(valor = novo) },
                            onRemover = { itens.removeAt(indice) },
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = itemPersonalizado,
                    onValueChange = {
                        if (it.length <= LIMITE_DESCRICAO_CUSTO) itemPersonalizado = it
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.item_personalizado_hint)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        itens.add(ItemCusto(itemPersonalizado.trim(), Icons.Outlined.Sell))
                        itemPersonalizado = ""
                    },
                    enabled = itemPersonalizado.isNotBlank() && itens.size < LIMITE_ITENS,
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.adicionar_item_cd)
                    )
                }
            }

            if (total > 0) ResumoOrcamento(itens = itens, total = total)

            SecaoLabelComIcone(
                texto = stringResource(R.string.observacoes_opcional),
                icone = Icons.AutoMirrored.Outlined.StickyNote2,
            )
            OutlinedTextField(
                value = observacoes,
                onValueChange = { if (it.length <= LIMITE_OBSERVACAO) observacoes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = { Text(stringResource(R.string.observacoes_hint)) },
                supportingText = { Text("${observacoes.length}/$LIMITE_OBSERVACAO") },
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.size(2.dp))
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            estado.erroAcao?.let { TextoErroAcao(it) }
            Button(
                onClick = {
                    val diaEscolhido = dia ?: return@Button
                    val horaInicio = inicio ?: return@Button
                    val horaFim = fim ?: return@Button
                    onEnviar(
                        montarDataHoraIso(diaEscolhido, horaInicio),
                        montarDataHoraIso(diaEscolhido, horaFim),
                        observacoes,
                        custos,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = podeEnviar,
            ) {
                if (estado.salvando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatarBRL(total.takeIf { it > 0 })
                        ?.let { "${stringResource(R.string.enviar_orcamento)} · $it" }
                        ?: stringResource(R.string.enviar_orcamento),
                )
            }
        }
    }
}

@Composable
private fun ItemCustoRow(item: ItemCusto, onValor: (String) -> Unit, onRemover: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            item.icone,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = item.nome,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "R$",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        OutlinedTextField(
            value = item.valor,
            onValueChange = onValor,
            modifier = Modifier.width(96.dp),
            placeholder = { Text("0") },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        IconButton(onClick = onRemover) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = stringResource(R.string.remover_item_cd),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ResumoOrcamento(itens: List<ItemCusto>, total: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            itens.filter { paraNumero(it.valor) > 0 }.forEach { item ->
                LinhaValor(
                    rotulo = item.nome,
                    valor = formatarBRL(paraNumero(item.valor)).orEmpty(),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Verde.copy(alpha = 0.10f))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Paid, null, tint = Verde, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.total_do_orcamento),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = formatarBRL(total).orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = Verde,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CalendarioServico(
    horarios: HorariosUi,
    dias: List<DiaHorariosRS>,
    diaAtual: DiaHorariosRS?,
    diaSelecionado: String?,
    inicioSelecionado: String?,
    fimSelecionado: String?,
    opcoesFim: List<String>,
    onSelecionarDia: (String) -> Unit,
    onSelecionarInicio: (String) -> Unit,
    onSelecionarFim: (String) -> Unit,
    onSemanaAnterior: () -> Unit,
    onProximaSemana: () -> Unit,
    onTentarNovamente: () -> Unit,
) {
    val semana = horarios.semana
    if (semana == null) {
        when {
            horarios.carregando -> CaixaCarregandoOrcamento()
            else -> ConteudoErro(
                mensagem = horarios.erro ?: stringResource(R.string.sem_agenda_servico),
                onTentarNovamente = onTentarNovamente,
            )
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NavegacaoSemana(
                rotulo = rotuloSemana(semana.inicioSemana, semana.fimSemana),
                carregando = horarios.carregando,
                podeVoltar = horarios.podeVoltarSemana && !horarios.carregando,
                onAnterior = onSemanaAnterior,
                onProxima = onProximaSemana,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dias.forEach { item ->
                    val data = item.data ?: return@forEach
                    val disponivel = item.horariosDisponiveis.isNotEmpty()
                    PilulaDia(
                        abreviatura = abrevDiaSemana(data),
                        diaMes = diaDoMes(data),
                        selecionado = data == diaSelecionado,
                        disponivel = disponivel,
                        onClick = { if (disponivel) onSelecionarDia(data) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            when {
                diaAtual == null -> TextoAuxiliar(stringResource(R.string.selecione_dia_horarios))
                diaAtual.horariosDisponiveis.isEmpty() ->
                    TextoAuxiliar(stringResource(R.string.sem_horarios_dia))

                else -> {
                    SecaoLabel(stringResource(R.string.hora_inicio))
                    LinhaHorarios(
                        horarios = diaAtual.horariosDisponiveis,
                        selecionado = inicioSelecionado,
                        onSelecionar = onSelecionarInicio,
                    )
                    SecaoLabel(stringResource(R.string.hora_fim))
                    if (inicioSelecionado == null) {
                        TextoAuxiliar(stringResource(R.string.selecione_inicio_primeiro))
                    } else {
                        LinhaHorarios(
                            horarios = opcoesFim,
                            selecionado = fimSelecionado,
                            onSelecionar = onSelecionarFim,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavegacaoSemana(
    rotulo: String,
    carregando: Boolean,
    podeVoltar: Boolean,
    onAnterior: () -> Unit,
    onProxima: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SetaSemana(
            icone = Icons.Filled.ChevronLeft,
            descricao = stringResource(R.string.semana_anterior),
            habilitado = podeVoltar,
            onClick = onAnterior,
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (carregando) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            }
        }
        SetaSemana(
            icone = Icons.Filled.ChevronRight,
            descricao = stringResource(R.string.proxima_semana),
            habilitado = !carregando,
            onClick = onProxima,
        )
    }
}

@Composable
private fun SetaSemana(
    icone: ImageVector,
    descricao: String,
    habilitado: Boolean,
    onClick: () -> Unit,
) {
    val cor = if (habilitado) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .then(if (habilitado) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, descricao, tint = cor, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun PilulaDia(
    abreviatura: String,
    diaMes: String,
    selecionado: Boolean,
    disponivel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fundo = when {
        selecionado -> MaterialTheme.colorScheme.primary
        disponivel -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }
    val corTexto = when {
        selecionado -> MaterialTheme.colorScheme.onPrimary
        disponivel -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(fundo)
            .then(if (disponivel) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(abreviatura, style = MaterialTheme.typography.labelSmall, color = corTexto)
        Text(diaMes, style = MaterialTheme.typography.titleSmall, color = corTexto)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinhaHorarios(
    horarios: List<String>,
    selecionado: String?,
    onSelecionar: (String) -> Unit,
) {
    if (horarios.isEmpty()) {
        TextoAuxiliar(stringResource(R.string.sem_horarios_dia))
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        horarios.forEach { hora ->
            val ativo = hora == selecionado
            Text(
                text = formatarHoraSlot(hora),
                style = MaterialTheme.typography.labelLarge,
                color = if (ativo) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (ativo) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable { onSelecionar(hora) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/* Conteúdo: recusar/cancelar (mesmo endpoint, motivo + descrição)    */
/* ------------------------------------------------------------------ */

@Composable
private fun ConteudoCancelar(
    estado: OrcamentoProDetalheUi,
    onFechar: () -> Unit,
    onCancelar: (String, String) -> Unit,
) {
    // Enquanto a solicitação está pendente é uma recusa; depois disso, cancelamento.
    val recusa = StatusOrcamento.de(estado.detalhe?.status) == StatusOrcamento.Pendente

    CabecalhoSheet(
        titulo = stringResource(if (recusa) R.string.recusar_solicitacao else R.string.cancelar_servico),
        subtitulo = stringResource(R.string.cancelar_servico_sub),
        onFechar = onFechar,
    )

    Spacer(Modifier.size(16.dp))
    estado.detalhe?.let { CartaoCliente(it, comStatus = false) }

    Spacer(Modifier.size(16.dp))
    FormularioCancelamento(
        motivos = MOTIVOS_CANCELAMENTO_PROFISSIONAL,
        rotuloConfirmar = stringResource(
            if (recusa) R.string.confirmar_recusa else R.string.confirmar_cancelamento,
        ),
        salvando = estado.salvando,
        erro = estado.erroAcao,
        onVoltar = onFechar,
        onConfirmar = onCancelar,
    )
}

/* ------------------------------------------------------------------ */
/* Conteúdo: concluir serviço                                         */
/* ------------------------------------------------------------------ */

@Composable
private fun ConteudoConcluir(
    estado: OrcamentoProDetalheUi,
    detalhe: OrcamentoDetalheProfissionalRS,
    onFechar: () -> Unit,
    onConcluir: (String) -> Unit,
) {
    var observacao by rememberSaveable { mutableStateOf("") }

    CabecalhoSheet(
        icone = Icons.Outlined.TaskAlt,
        titulo = stringResource(R.string.concluir_servico),
        subtitulo = stringResource(R.string.concluir_servico_sub),
        onFechar = onFechar,
    )

    Spacer(Modifier.size(16.dp))
    CartaoCliente(detalhe, comStatus = false)

    detalhe.orcamentoFinal?.valorTotal?.let { valor ->
        Spacer(Modifier.size(12.dp))
        CaixaInfo(
            icone = Icons.Outlined.Paid,
            rotulo = stringResource(R.string.valor_aprovado),
            valor = formatarBRL(valor).orEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(Modifier.size(16.dp))
    SecaoLabel(stringResource(R.string.observacao_conclusao))
    Spacer(Modifier.size(8.dp))
    OutlinedTextField(
        value = observacao,
        onValueChange = { if (it.length <= LIMITE_OBSERVACAO) observacao = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        placeholder = { Text(stringResource(R.string.observacao_conclusao_hint)) },
        supportingText = { Text("${observacao.length}/$LIMITE_OBSERVACAO") },
        shape = RoundedCornerShape(12.dp),
        enabled = !estado.salvando,
    )

    estado.erroAcao?.let {
        Spacer(Modifier.size(8.dp))
        TextoErroAcao(it)
    }

    Spacer(Modifier.size(16.dp))
    Button(
        onClick = { onConcluir(observacao) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !estado.salvando,
    ) {
        if (estado.salvando) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Icon(Icons.Outlined.TaskAlt, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.concluir_servico))
        }
    }
}

/* ------------------------------------------------------------------ */
/* Conteúdo: avaliar cliente                                          */
/* ------------------------------------------------------------------ */

@Composable
private fun ConteudoAvaliar(
    estado: OrcamentoProDetalheUi,
    detalhe: OrcamentoDetalheProfissionalRS,
    onFechar: () -> Unit,
    onAvaliar: (Int, String) -> Unit,
) {
    CabecalhoSheet(
        icone = Icons.Outlined.StarBorder,
        titulo = stringResource(R.string.avaliar_cliente),
        onFechar = onFechar,
    )

    Spacer(Modifier.size(16.dp))
    CartaoCliente(detalhe, comStatus = false)

    Spacer(Modifier.size(16.dp))
    FormularioAvaliacao(
        nome = detalhe.cliente?.nome.orEmpty(),
        salvando = estado.salvando,
        erro = estado.erroAcao,
        onPublicar = onAvaliar,
    )
}

/* ------------------------------------------------------------------ */
/* Peças reutilizadas                                                 */
/* ------------------------------------------------------------------ */

/** Faixa com foto, nome, nota do cliente e (opcionalmente) o status do orçamento. */
@Composable
private fun CartaoCliente(detalhe: OrcamentoDetalheProfissionalRS, comStatus: Boolean) {
    val cliente = detalhe.cliente
    val nome = cliente?.nome.orEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarPerfil(
            nome = nome,
            fotoUrl = cliente?.fotoPerfil,
            tamanho = 44.dp,
            fonte = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                cliente?.avaliacao?.let { nota ->
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.Star,
                        null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${formatarNota(nota)} · ${cliente.quantidadeAvaliacoes ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = stringResource(R.string.cliente),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (comStatus) StatusBadge(StatusOrcamento.de(detalhe.status), detalhe.status)
    }
}

@Composable
private fun GaleriaFotos(imagens: List<String>) {
    if (imagens.isEmpty()) return
    Spacer(Modifier.size(16.dp))
    SecaoLabel(stringResource(R.string.fotos_do_cliente))
    Spacer(Modifier.size(10.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(imagens) { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
private fun SecaoLabelComIcone(texto: String, icone: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        SecaoLabel(texto)
    }
}

@Composable
private fun ChipCategoria(
    icone: ImageVector,
    texto: String,
    ativa: Boolean,
    onClick: () -> Unit,
) {
    val cor = if (ativa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .then(
                if (ativa) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icone, null, tint = cor, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.labelMedium, color = cor)
    }
}


@Composable
private fun TextoAuxiliar(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TextoErroAcao(mensagem: String) {
    Text(
        text = mensagem,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/* ---------------------------- Cálculos e datas ---------------------------- */

/** Converte o texto digitado (ex: "1.500,50") em número. */
private fun paraNumero(texto: String): Double {
    val limpo = texto.replace(".", "").replace(",", ".").filter { it.isDigit() || it == '.' }
    return limpo.toDoubleOrNull() ?: 0.0
}

/**
 * Horários possíveis para o fim do serviço: os slots do dia após o início escolhido.
 * Sem slots posteriores, propõe um único horário a um intervalo do início.
 */
private fun opcoesDeFim(
    dia: DiaHorariosRS?,
    inicio: String?,
    intervaloMinutos: Int?,
): List<String> {
    if (dia == null || inicio == null) return emptyList()
    val horaInicio = runCatching { LocalTime.parse(inicio) }.getOrNull() ?: return emptyList()
    val posteriores = dia.horariosDisponiveis.filter { hora ->
        runCatching { LocalTime.parse(hora).isAfter(horaInicio) }.getOrDefault(false)
    }
    if (posteriores.isNotEmpty()) return posteriores

    val fimSugerido = horaInicio.plusMinutes((intervaloMinutos ?: 60).toLong())
    return if (fimSugerido.isAfter(horaInicio)) listOf(fimSugerido.toString()) else emptyList()
}

private val DIAS_SEMANA_ABREV = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

private val MESES_ABREV =
    listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

private fun abrevDiaSemana(dataIso: String): String =
    runCatching { DIAS_SEMANA_ABREV[LocalDate.parse(dataIso).dayOfWeek.value - 1] }.getOrDefault("")

private fun diaDoMes(dataIso: String): String =
    runCatching { LocalDate.parse(dataIso).dayOfMonth.toString() }.getOrDefault("")

@Composable
private fun rotuloSemana(inicioIso: String?, fimIso: String?): String {
    val inicio = inicioIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val fim = fimIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    if (inicio == null || fim == null) return stringResource(R.string.esta_semana)
    return if (inicio.monthValue == fim.monthValue) {
        "${inicio.dayOfMonth} – ${fim.dayOfMonth} de ${MESES_ABREV[fim.monthValue - 1]}"
    } else {
        "${inicio.dayOfMonth} ${MESES_ABREV[inicio.monthValue - 1]} – " +
                "${fim.dayOfMonth} ${MESES_ABREV[fim.monthValue - 1]}"
    }
}
