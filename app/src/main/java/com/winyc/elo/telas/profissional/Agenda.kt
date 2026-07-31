package com.winyc.elo.telas.profissional

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.winyc.elo.backend.viewModel.AgendaUi
import com.winyc.elo.backend.viewModel.AgendaViewModel
import com.winyc.elo.backend.viewModel.ServicoAgendaUi
import com.winyc.elo.backend.viewModel.StatusAgenda
import com.winyc.elo.telas.componentes.AvatarPerfil
import com.winyc.elo.telas.componentes.DIAS_DA_SEMANA
import com.winyc.elo.telas.componentes.diaPorExtenso
import com.winyc.elo.telas.componentes.faixaDeHorario
import com.winyc.elo.telas.componentes.formatarBRL
import com.winyc.elo.telas.componentes.formatarDistancia
import com.winyc.elo.telas.componentes.formatarHora
import com.winyc.elo.telas.componentes.formatarNota
import com.winyc.elo.telas.componentes.mesEAno
import java.time.LocalDate

private val Ambar = Color(0xFFF5A524)
private val SobreAmbar = Color(0xFF16181F)
private const val MAX_MARCADORES = 3

private data class DiaEmExibicao(val dia: LocalDate, val servicos: List<ServicoAgendaUi>)

@Composable
fun AgendaScreen(
    onIrParaOrcamentos: () -> Unit,
    modifier: Modifier = Modifier,
    vm: AgendaViewModel = viewModel(),
) {
    val estado by vm.estado.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        CabecalhoAgenda(
            estado = estado,
            onSelecionarDia = vm::selecionarDia,
            onSemanaAnterior = vm::semanaAnterior,
            onProximaSemana = vm::proximaSemana,
            onIrParaHoje = vm::irParaHoje,
        )

        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ResumoSemana(estado)

            AnimatedContent(
                targetState = DiaEmExibicao(estado.diaSelecionado, estado.servicosDoDia),
                transitionSpec = {
                    val avancando = targetState.dia > initialState.dia
                    val sentido = if (avancando) 1 else -1
                    val entrada = slideInHorizontally(tween(220)) { largura -> sentido * largura / 5 } +
                        fadeIn(tween(220))
                    val saida = slideOutHorizontally(tween(180)) { largura -> -sentido * largura / 5 } +
                        fadeOut(tween(140))
                    entrada togetherWith saida using SizeTransform(clip = false)
                },
                label = "servicos-do-dia",
            ) { emExibicao ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CabecalhoDia(dia = emExibicao.dia, hoje = estado.hoje)
                    if (emExibicao.servicos.isEmpty()) {
                        DiaSemServicos(onIrParaOrcamentos)
                    } else {
                        emExibicao.servicos.forEach { servico ->
                            CardServico(servico = servico, onResponder = onIrParaOrcamentos)
                        }
                    }
                }
            }
        }
    }
}

/* ---------------------------- Cabeçalho e faixa da semana ---------------------------- */

@Composable
private fun CabecalhoAgenda(
    estado: AgendaUi,
    onSelecionarDia: (LocalDate) -> Unit,
    onSemanaAnterior: () -> Unit,
    onProximaSemana: () -> Unit,
    onIrParaHoje: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.agenda_titulo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
                Text(
                    text = mesEAno(estado.diaSelecionado),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            BotaoHoje(onClick = onIrParaHoje)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            SetaSemana(
                icone = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                descricao = stringResource(R.string.agenda_semana_anterior_cd),
                onClick = onSemanaAnterior,
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                estado.dias.forEachIndexed { indice, dia ->
                    CelulaDia(
                        rotulo = DIAS_DA_SEMANA[indice],
                        dia = dia,
                        selecionado = dia == estado.diaSelecionado,
                        ehHoje = dia == estado.hoje,
                        servicos = estado.servicosDe(dia),
                        onClick = { onSelecionarDia(dia) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            SetaSemana(
                icone = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                descricao = stringResource(R.string.agenda_proxima_semana_cd),
                onClick = onProximaSemana,
            )
        }
    }
}

@Composable
private fun BotaoHoje(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.agenda_hoje),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SetaSemana(icone: ImageVector, descricao: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icone,
            contentDescription = descricao,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun CelulaDia(
    rotulo: String,
    dia: LocalDate,
    selecionado: Boolean,
    ehHoje: Boolean,
    servicos: List<ServicoAgendaUi>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val corTexto = MaterialTheme.colorScheme.onPrimary
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (selecionado) MaterialTheme.colorScheme.background else Color.Transparent)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.labelSmall,
                color = if (selecionado) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    corTexto.copy(alpha = 0.8f)
                },
            )
            Text(
                text = dia.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (selecionado) MaterialTheme.colorScheme.onBackground else corTexto,
            )
        }

        Spacer(Modifier.height(4.dp))
        MarcadoresDoDia(servicos)
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(2.dp)
                .clip(CircleShape)
                .background(if (ehHoje) corTexto else Color.Transparent),
        )
    }
}
@Composable
private fun MarcadoresDoDia(servicos: List<ServicoAgendaUi>) {
    Row(
        modifier = Modifier.height(5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        servicos.take(MAX_MARCADORES).forEach { servico ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(corDoMarcador(servico.status)),
            )
        }
    }
}

@Composable
private fun corDoMarcador(status: StatusAgenda): Color = when (status) {
    StatusAgenda.Pendente, StatusAgenda.AguardandoCliente -> Ambar
    StatusAgenda.Confirmado -> MaterialTheme.colorScheme.onPrimary
    StatusAgenda.Concluido -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.45f)
}

/* ---------------------------- Resumo da semana ---------------------------- */

@Composable
private fun ResumoSemana(estado: AgendaUi) {
    Card(
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
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.agenda_previsto_semana),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatarBRL(estado.totalPrevistoSemana).orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = pluralStringResource(
                        R.plurals.agenda_quantidade_servicos,
                        estado.quantidadeSemana,
                        estado.quantidadeSemana,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (estado.aguardandoSemana > 0) {
                    Text(
                        text = stringResource(R.string.agenda_aguardando, estado.aguardandoSemana),
                        style = MaterialTheme.typography.bodySmall,
                        color = Ambar,
                    )
                }
            }
        }
    }
}

/* ---------------------------- Serviços do dia ---------------------------- */

@Composable
private fun CabecalhoDia(dia: LocalDate, hoje: LocalDate) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = diaPorExtenso(dia),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (dia == hoje) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.agenda_hoje_marcador),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun CardServico(servico: ServicoAgendaUi, onResponder: () -> Unit) {
    val visual = visualDoStatus(servico.status)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = visual.destaque?.let { BorderStroke(1.dp, it) },
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LinhaHorario(servico)
            LinhaCliente(servico)

            servico.descricao?.takeIf { it.isNotBlank() }?.let { descricao ->
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            LinhaLocalEStatus(servico, visual)

            if (servico.status == StatusAgenda.Pendente) {
                Button(onClick = onResponder, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.agenda_responder_agora))
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LinhaHorario(servico: ServicoAgendaUi) {
    val horario = if (servico.temProposta) {
        faixaDeHorario(servico.inicioIso, servico.fimIso)
    } else {
        formatarHora(servico.horarioPreferidoIso)
    }
    val valor = formatarBRL(servico.valorTotal)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = horario.orEmpty(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!servico.temProposta) {
            Text(
                text = " · ${stringResource(R.string.agenda_preferencia_cliente)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.weight(1f))
        if (valor != null) {
            Text(
                text = valor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            Text(
                text = stringResource(R.string.agenda_status_pendente),
                style = MaterialTheme.typography.bodySmall,
                color = Ambar,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun LinhaCliente(servico: ServicoAgendaUi) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AvatarPerfil(
            nome = servico.nomeCliente,
            fotoUrl = servico.fotoCliente,
            tamanho = 28.dp,
            fonte = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = servico.nomeCliente,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        servico.avaliacaoCliente?.let { nota ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = formatarNota(nota),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LinhaLocalEStatus(servico: ServicoAgendaUi, visual: VisualStatus) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val local = listOfNotNull(
            servico.bairro?.takeIf { it.isNotBlank() },
            formatarDistancia(servico.distanciaKm),
        ).joinToString(" · ")

        if (local.isNotBlank()) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = local,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.weight(1f))
        ChipStatus(visual)
    }
}

@Composable
private fun ChipStatus(visual: VisualStatus) {
    Text(
        text = stringResource(visual.rotuloRes),
        style = MaterialTheme.typography.labelSmall,
        color = visual.corTexto,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(CircleShape)
            .background(visual.corFundo)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun DiaSemServicos(onIrParaOrcamentos: () -> Unit) {
    val corBorda = MaterialTheme.colorScheme.outline
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = corBorda,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f)),
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                )
            }
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = stringResource(R.string.agenda_vazio_titulo),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.agenda_vazio_texto),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onIrParaOrcamentos)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = stringResource(R.string.agenda_ver_orcamentos),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/* ---------------------------- Aparência por status ---------------------------- */

private data class VisualStatus(
    val rotuloRes: Int,
    val corTexto: Color,
    val corFundo: Color,
    val destaque: Color?,
)

@Composable
private fun visualDoStatus(status: StatusAgenda): VisualStatus {
    val primaria = MaterialTheme.colorScheme.primary
    val suave = MaterialTheme.colorScheme.onSurfaceVariant
    return when (status) {
        StatusAgenda.Confirmado -> VisualStatus(
            rotuloRes = R.string.agenda_chip_confirmado,
            corTexto = MaterialTheme.colorScheme.onPrimary,
            corFundo = primaria,
            destaque = primaria,
        )

        StatusAgenda.Pendente -> VisualStatus(
            rotuloRes = R.string.agenda_chip_aguardando_resposta,
            corTexto = SobreAmbar,
            corFundo = Ambar,
            destaque = Ambar,
        )

        StatusAgenda.AguardandoCliente -> VisualStatus(
            rotuloRes = R.string.agenda_chip_aguardando_cliente,
            corTexto = Ambar,
            corFundo = Ambar.copy(alpha = 0.16f),
            destaque = null,
        )

        StatusAgenda.Concluido -> VisualStatus(
            rotuloRes = R.string.agenda_chip_concluido,
            corTexto = suave,
            corFundo = MaterialTheme.colorScheme.surfaceVariant,
            destaque = null,
        )
    }
}