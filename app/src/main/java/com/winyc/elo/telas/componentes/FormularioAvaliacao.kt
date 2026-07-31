package com.winyc.elo.telas.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.winyc.elo.R

private const val LIMITE_COMENTARIO = 200

/** Estrelas (1 a 5) + comentário opcional exigidos pela API ao avaliar. */
@Composable
fun FormularioAvaliacao(
    nome: String,
    salvando: Boolean,
    erro: String?,
    onPublicar: (nota: Int, comentario: String) -> Unit,
) {
    var nota by rememberSaveable { mutableIntStateOf(5) }
    var comentario by rememberSaveable { mutableStateOf("") }

    Text(
        text = stringResource(R.string.experiencia_com, nome),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(Modifier.size(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        (1..5).forEach { indice ->
            val ativa = indice <= nota
            Icon(
                imageVector = if (ativa) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = stringResource(R.string.estrelas_cd, indice),
                tint = if (ativa) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !salvando) { nota = indice },
            )
        }
    }

    Spacer(Modifier.size(16.dp))
    OutlinedTextField(
        value = comentario,
        onValueChange = { if (it.length <= LIMITE_COMENTARIO) comentario = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        placeholder = { Text(stringResource(R.string.avaliacao_placeholder)) },
        supportingText = { Text("${comentario.length}/$LIMITE_COMENTARIO") },
        shape = RoundedCornerShape(12.dp),
        enabled = !salvando,
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
    Button(
        onClick = { onPublicar(nota, comentario.trim()) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !salvando,
    ) {
        if (salvando) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.publicar_avaliacao))
        }
    }
}
