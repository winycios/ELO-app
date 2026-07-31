package com.winyc.elo.telas.componentes

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winyc.elo.R

private const val LIMITE_MOTIVO = 50

private const val LIMITE_DESCRICAO = 200

/** Motivos oferecidos ao cliente que desiste do serviço. */
val MOTIVOS_CANCELAMENTO_CLIENTE = listOf(
    R.string.motivo_resolvido,
    R.string.motivo_valor_alto,
    R.string.motivo_data_nao_serve,
    R.string.motivo_outro_profissional,
    R.string.motivo_imprevisto,
    R.string.motivo_outro,
)

/** Motivos oferecidos ao profissional que recusa ou cancela o serviço. */
val MOTIVOS_CANCELAMENTO_PROFISSIONAL = listOf(
    R.string.motivo_agenda,
    R.string.motivo_imprevisto,
    R.string.motivo_fora_area,
    R.string.motivo_fora_escopo,
    R.string.motivo_sem_resposta,
    R.string.motivo_outro,
)

/**
 * Motivo (obrigatório) + descrição livre (obrigatória) exigidos pela API ao cancelar
 * um orçamento, com os botões de confirmar e voltar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormularioCancelamento(
    motivos: List<Int>,
    rotuloConfirmar: String,
    salvando: Boolean,
    erro: String?,
    onVoltar: () -> Unit,
    onConfirmar: (motivo: String, descricao: String) -> Unit,
) {
    var motivo by rememberSaveable { mutableStateOf<String?>(null) }
    var descricao by rememberSaveable { mutableStateOf("") }

    RotuloCampo(R.string.motivo_cancelamento)
    Spacer(Modifier.size(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        motivos.forEach { rotuloRes ->
            val rotulo = stringResource(rotuloRes)
            ChipMotivo(
                texto = rotulo,
                ativo = rotulo == motivo,
                onClick = { motivo = rotulo },
            )
        }
    }

    Spacer(Modifier.size(16.dp))
    RotuloCampo(R.string.descricao_cancelamento)
    Spacer(Modifier.size(8.dp))
    OutlinedTextField(
        value = descricao,
        onValueChange = { if (it.length <= LIMITE_DESCRICAO) descricao = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        placeholder = { Text(stringResource(R.string.descricao_cancelamento_hint)) },
        supportingText = { Text("${descricao.length}/$LIMITE_DESCRICAO") },
        shape = RoundedCornerShape(12.dp),
    )

    if (erro != null) {
        Spacer(Modifier.size(8.dp))
        Text(
            text = erro,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }

    Spacer(Modifier.size(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onVoltar,
            modifier = Modifier.weight(1f),
            enabled = !salvando,
        ) {
            Text(stringResource(R.string.voltar_sem_cancelar))
        }
        Button(
            onClick = { onConfirmar(motivo.orEmpty().take(LIMITE_MOTIVO), descricao) },
            modifier = Modifier.weight(1f),
            enabled = motivo != null && descricao.isNotBlank() && !salvando,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            if (salvando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onError,
                )
            } else {
                Text(rotuloConfirmar)
            }
        }
    }
}

@Composable
private fun RotuloCampo(@StringRes texto: Int) {
    Text(
        text = stringResource(texto).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun ChipMotivo(texto: String, ativo: Boolean, onClick: () -> Unit) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = if (ativo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clip(CircleShape)
            .then(
                if (ativo) Modifier.background(MaterialTheme.colorScheme.primary)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
