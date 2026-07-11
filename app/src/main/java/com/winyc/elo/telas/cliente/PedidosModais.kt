package com.winyc.elo.telas.cliente

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winyc.elo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.core.net.toUri

private val Verde = Color(0xFF12A15A)

/** Anima o fechamento do sheet e só então limpa o estado no chamador. */
@OptIn(ExperimentalMaterial3Api::class)
private fun esconderEntao(scope: CoroutineScope, sheetState: SheetState, aoFim: () -> Unit) {
    scope.launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) aoFim()
    }
}

/* ------------------------------------------------------------------ */
/* Modal: Contato                                                     */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContatoSheet(
    nome: String,
    subtitulo: String,
    telefone: String,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val digitos = telefone.filter { it.isDigit() }
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        modifier = Modifier.padding(bottom = 120.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CabecalhoSheet(
                icone = Icons.Outlined.Phone,
                titulo = stringResource(R.string.contato),
                onFechar = { esconderEntao(scope, sheetState, onFechar) },
            )

            Spacer(Modifier.size(16.dp))
            AvatarComStatusOnline(nome)

            Spacer(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nome,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Verificado",
                    tint = Verde,
                    modifier = Modifier.size(18.dp),
                )
            }
            CategoriaChip(subtitulo)

            Spacer(Modifier.size(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.numero_de_telefone),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = telefone,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            clipboard.setClipEntry(
                                ClipData.newPlainText(
                                    "Telefone", telefone
                                ).toClipEntry()
                            )
                        }
                        Toast.makeText(context, "Número copiado", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar número",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.size(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        context.abrir(
                            Intent(
                                Intent.ACTION_DIAL,
                                "tel:$digitos".toUri()
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.Phone, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.ligar))
                }
                Button(
                    onClick = {
                        context.abrir(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://wa.me/55$digitos".toUri()
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.whatsapp))
                }
            }

            Spacer(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = Verde,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.numero_verificado_plataforma),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Modal: Detalhes do pedido                                          */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetalhesPedidoSheet(
    pedido: Pedido,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        modifier = Modifier.padding(bottom = 120.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            CabecalhoSheet(
                titulo = stringResource(R.string.detalhes_do_pedido),
                onFechar = { esconderEntao(scope, sheetState, onFechar) },
            )

            Spacer(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPedido(pedido.profissional, tamanho = 44.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = pedido.profissional,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(pedido.status)
            }

            Spacer(Modifier.size(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.size(16.dp))

            SecaoLabel(stringResource(R.string.informacoes_da_solicitacao))
            Spacer(Modifier.size(12.dp))
            InfoLinha(
                Icons.Outlined.Description,
                stringResource(R.string.descricao),
                pedido.servico
            )
            InfoLinha(
                Icons.Outlined.LocalOffer,
                stringResource(R.string.categoria),
                pedido.categoria
            )
            InfoLinha(Icons.Outlined.CalendarToday, stringResource(R.string.data), pedido.data)
            InfoLinha(Icons.Outlined.Paid, stringResource(R.string.valor), pedido.total)

            Spacer(Modifier.size(20.dp))
            Button(
                onClick = { esconderEntao(scope, sheetState, onFechar) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.fechar))
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Modal: Orçamento final                                             */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrcamentoFinalSheet(
    pedido: Pedido,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onFechar, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            CabecalhoSheet(
                icone = Icons.Outlined.EditNote,
                titulo = stringResource(R.string.orcamento_final),
                subtitulo = stringResource(R.string.revise_e_decida),
                onFechar = { esconderEntao(scope, sheetState, onFechar) },
            )

            Spacer(Modifier.size(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPedido(pedido.profissional, tamanho = 52.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pedido.profissional,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verificado",
                            tint = Verde,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = pedido.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${pedido.avaliacao} · ${pedido.numAvaliacoes} ${stringResource(R.string.avaliacoes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.size(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.size(16.dp))

            SecaoLabel(stringResource(R.string.servico))
            Spacer(Modifier.size(6.dp))
            Text(
                text = pedido.servico,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.size(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CaixaInfo(
                    icone = Icons.Outlined.CalendarToday,
                    rotulo = stringResource(R.string.data),
                    valor = pedido.data,
                    modifier = Modifier.weight(1f),
                )
                CaixaInfo(
                    icone = Icons.Outlined.Schedule,
                    rotulo = stringResource(R.string.horario),
                    valor = pedido.horario,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.size(10.dp))
            CaixaInfo(
                icone = Icons.Outlined.LocationOn,
                rotulo = null,
                valor = pedido.endereco,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.size(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.size(16.dp))

            SecaoLabel(stringResource(R.string.valor))
            Spacer(Modifier.size(10.dp))
            LinhaValor(stringResource(R.string.mao_de_obra), pedido.maoDeObra)
            LinhaValor(stringResource(R.string.material_incluso), pedido.material)
            LinhaValor(stringResource(R.string.deslocamento), pedido.deslocamento)
            Spacer(Modifier.size(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.size(8.dp))
            LinhaValor(stringResource(R.string.total), pedido.total, destaque = true)

            Spacer(Modifier.size(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Garantia de 90 dias. Trabalho finalizado no mesmo dia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.size(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { esconderEntao(scope, sheetState, onFechar) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.recusar))
                }
                Button(
                    onClick = {
                        Toast.makeText(context, "Orçamento aceito!", Toast.LENGTH_SHORT).show()
                        esconderEntao(scope, sheetState, onFechar)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.aceitar_orcamento))
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Modal: Avaliar                                                     */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AvaliarSheet(
    nome: String,
    onFechar: () -> Unit,
    titulo: String = "Avaliar profissional",
    onPublicar: (nota: Int, comentario: String) -> Unit = { _, _ -> },
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var nota by rememberSaveable { mutableIntStateOf(5) }
    var comentario by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onFechar, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
                        contentDescription = "$indice estrela(s)",
                        tint = if (ativa) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { nota = indice },
                    )
                }
            }

            Spacer(Modifier.size(16.dp))
            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = { Text(stringResource(R.string.avaliacao_placeholder)) },
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.size(20.dp))
            Button(
                onClick = {
                    onPublicar(nota, comentario.trim())
                    Toast.makeText(context, "Avaliação publicada!", Toast.LENGTH_SHORT).show()
                    esconderEntao(scope, sheetState, onFechar)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.publicar_avaliacao))
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Peças reutilizadas pelos modais                                    */
/* ------------------------------------------------------------------ */

/** Cabeçalho padrão: título (com ícone/subtítulo opcionais) + botão de fechar. */
@Composable
private fun CabecalhoSheet(
    titulo: String,
    onFechar: () -> Unit,
    icone: ImageVector? = null,
    subtitulo: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icone != null) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(
            onClick = onFechar,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Fechar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun AvatarComStatusOnline(nome: String) {
    Box {
        AvatarPedido(nome, tamanho = 72.dp)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(2.dp)
                .clip(CircleShape)
                .background(Verde),
        )
    }
}

@Composable
private fun CategoriaChip(categoria: String) {
    Text(
        text = categoria,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun SecaoLabel(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )
}

/** Linha "ícone + rótulo + valor" usada nos detalhes do pedido. */
@Composable
private fun InfoLinha(icone: ImageVector, rotulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Caixa destacada (fundo do contexto) para data/horário/endereço no orçamento. */
@Composable
private fun CaixaInfo(
    icone: ImageVector,
    rotulo: String?,
    valor: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp),
            )
            if (rotulo != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = rotulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.size(4.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Linha de valor (rótulo à esquerda, valor à direita); total fica em destaque. */
@Composable
private fun LinhaValor(rotulo: String, valor: String, destaque: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = rotulo,
            style = if (destaque) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (destaque) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = valor,
            style = if (destaque) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (destaque) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (destaque) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/** Abre uma Intent com segurança (ignora se não houver app para tratar). */
private fun android.content.Context.abrir(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(this, "Nenhum app disponível", Toast.LENGTH_SHORT).show()
    }
}
