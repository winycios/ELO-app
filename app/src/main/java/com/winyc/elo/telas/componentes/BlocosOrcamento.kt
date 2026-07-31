package com.winyc.elo.telas.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winyc.elo.R
import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheRS

private val VerdeConclusao = Color(0xFF12A15A)

private val VermelhoCancelamento = Color(0xFFF2603E)

/** Motivo, detalhes e autor do cancelamento, exibidos nos detalhes do orçamento. */
@Composable
fun BlocoCancelamento(cancelamento: OrcamentoDetalheRS.CancelamentoRS) {
    CaixaDestacada(
        cor = VermelhoCancelamento,
        icone = Icons.Outlined.Cancel,
        titulo = stringResource(R.string.cancelamento),
        rodape = rotuloAutor(cancelamento.autor)?.let { stringResource(R.string.cancelado_por, it) },
        data = cancelamento.data,
    ) {
        cancelamento.motivo?.takeIf { it.isNotBlank() }?.let { motivo ->
            Text(
                text = motivo,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        cancelamento.descricao?.takeIf { it.isNotBlank() }?.let { descricao ->
            Text(
                text = descricao,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Observação deixada pelo profissional ao concluir o serviço. */
@Composable
fun BlocoConclusao(conclusao: OrcamentoDetalheRS.ConclusaoRS) {
    CaixaDestacada(
        cor = VerdeConclusao,
        icone = Icons.Outlined.CheckCircle,
        titulo = stringResource(R.string.conclusao_do_servico),
        rodape = null,
        data = conclusao.data,
    ) {
        conclusao.observacao?.takeIf { it.isNotBlank() }?.let { observacao ->
            Text(
                text = observacao,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CaixaDestacada(
    cor: Color,
    icone: ImageVector,
    titulo: String,
    rodape: String?,
    data: String?,
    conteudo: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cor.copy(alpha = 0.10f))
            .border(1.dp, cor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icone, null, tint = cor, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = titulo.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = cor,
                fontWeight = FontWeight.Medium,
            )
        }
        conteudo()
        val detalhe = listOfNotNull(rodape, formatarDataHora(data)).joinToString(" · ")
        if (detalhe.isNotBlank()) {
            Text(
                text = detalhe,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun rotuloAutor(autor: String?): String? = when (autor?.trim()?.lowercase()) {
    null, "" -> null
    "cliente", "usuario", "usuário" -> stringResource(R.string.autor_cliente)
    "profissional" -> stringResource(R.string.autor_profissional)
    else -> autor
}
