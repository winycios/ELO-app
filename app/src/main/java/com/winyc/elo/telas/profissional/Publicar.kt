package com.winyc.elo.telas.profissional

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp

private val SERVICOS = listOf(
    "Instalação elétrica residencial",
    "Manutenção e reparo elétrico",
    "Troca de disjuntores",
    "Instalação de chuveiro",
)

private data class ComentarioPub(val autor: String, val texto: String, val tempo: String)

private data class Publicacao(
    val categoria: String,
    val descricao: String,
    val curtidas: Int,
    val comentarios: List<ComentarioPub>,
    val tempo: String,
)

private val PUBLICACOES_INICIAIS = listOf(
    Publicacao(
        categoria = "Instalação elétrica residencial",
        descricao = "Mais uma instalação elétrica completa finalizada! Quadro novo com disjuntores individuais. ⚡",
        curtidas = 87,
        comentarios = listOf(
            ComentarioPub("Maria L.", "Trabalho impecável como sempre!", "2h"),
            ComentarioPub("João P.", "Você fez a minha casa também. Top!", "1h"),
            ComentarioPub("Lucas M.", "Quanto custa em média uma instalação assim?", "45min"),
        ),
        tempo = "3h",
    ),
    Publicacao(
        categoria = "Manutenção e reparo elétrico",
        descricao = "Dica rápida: sempre desligue o disjuntor geral antes de qualquer intervenção elétrica. Segurança em primeiro lugar!",
        curtidas = 142,
        comentarios = listOf(
            ComentarioPub("Ana C.", "Ótima dica!", "1d"),
            ComentarioPub("Pedro R.", "Salvou meu fim de semana 😅", "1d"),
        ),
        tempo = "2d",
    ),
)

/** Tela "Publicar": compõe um post e lista as publicações do profissional. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarScreen(modifier: Modifier = Modifier) {
    val publicacoes = remember { mutableStateListOf<Publicacao>().apply { addAll(PUBLICACOES_INICIAIS) } }
    var comentariosDe by remember { mutableStateOf<Publicacao?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Publicar",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Mostre seu trabalho na Vitrine",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item { ComporPublicacao() }

        item {
            Text(
                text = "Minhas publicações",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        items(publicacoes) { pub ->
            PublicacaoCard(
                pub = pub,
                onComentarios = { comentariosDe = pub },
                onExcluir = { publicacoes.remove(pub) },
            )
        }
    }

    comentariosDe?.let { pub ->
        ComentariosPublicacaoSheet(
            comentariosIniciais = pub.comentarios,
            onFechar = { comentariosDe = null },
        )
    }
}


@Composable
private fun ComporPublicacao() {
    val context = LocalContext.current
    var servico by rememberSaveable { mutableStateOf(SERVICOS.first()) }
    var imagemSelecionada by rememberSaveable { mutableIntStateOf(0) }
    var descricao by rememberSaveable { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocalOffer, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Sobre qual serviço?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SERVICOS) { s ->
                    val sel = s == servico
                    FilterChip(
                        selected = sel,
                        onClick = { servico = s },
                        label = { Text(s, fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = Color.Transparent,
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(44.dp))
            }

            Text("Imagens", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { i ->
                    Miniatura(
                        selecionada = i == imagemSelecionada,
                        onClick = { imagemSelecionada = i },
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .clickable { Toast.makeText(context, "Adicionar imagem", Toast.LENGTH_SHORT).show() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }

            TextField(
                value = descricao,
                onValueChange = { descricao = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                placeholder = { Text("Conte sobre esse trabalho…") },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )

            // Aviso de onde será publicado.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.LocalOffer, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    buildAnnotatedString {
                        append("Será publicado em ")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)) {
                            append(servico)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Button(
                onClick = { Toast.makeText(context, "Publicado na Vitrine!", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth(),
                enabled = descricao.isNotBlank(),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Publicar na Vitrine")
            }
        }
    }
}

@Composable
private fun Miniatura(selecionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (selecionada) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
    }
}


@Composable
private fun PublicacaoCard(pub: Publicacao, onComentarios: () -> Unit, onExcluir: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
            Text(
                text = pub.categoria,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(pub.descricao, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                MetricaIcone(Icons.Outlined.FavoriteBorder, pub.curtidas.toString(), onClick = null)
                Spacer(Modifier.width(16.dp))
                MetricaIcone(Icons.Outlined.ChatBubbleOutline, pub.comentarios.size.toString(), onClick = onComentarios)
                Spacer(Modifier.width(10.dp))
                Text(pub.tempo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onExcluir) {
                    Icon(Icons.Outlined.DeleteOutline, "Excluir publicação", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun MetricaIcone(icone: androidx.compose.ui.graphics.vector.ImageVector, valor: String, onClick: (() -> Unit)?) {
    val base = Modifier
        .clip(RoundedCornerShape(8.dp))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(4.dp)
    Row(modifier = base, verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(valor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* ---------------------------- Comentários ---------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComentariosPublicacaoSheet(
    comentariosIniciais: List<ComentarioPub>,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val comentarios = remember { mutableStateListOf<ComentarioPub>().apply { addAll(comentariosIniciais) } }
    var respondendo by remember { mutableStateOf<Int?>(null) }

    ModalBottomSheet(onDismissRequest = onFechar, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Comentários (${comentarios.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onFechar,
                    modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Icon(Icons.Outlined.Close, "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.size(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(comentarios.size) { i ->
                    ComentarioItem(
                        comentario = comentarios[i],
                        respondendo = respondendo == i,
                        onResponder = { respondendo = if (respondendo == i) null else i },
                        onEnviarResposta = { texto ->
                            comentarios.add(i + 1, ComentarioPub("Você", texto, "agora"))
                            respondendo = null
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ComentarioItem(
    comentario: ComentarioPub,
    respondendo: Boolean,
    onResponder: () -> Unit,
    onEnviarResposta: (String) -> Unit,
) {
    Row {
        AvatarCliente(comentario.autor, tamanho = 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Balão do comentário.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comentario.autor, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text(comentario.tempo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.size(2.dp))
                Text(comentario.texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onResponder)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Outlined.Reply, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Responder", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }

            if (respondendo) {
                CampoResposta(autor = comentario.autor, onEnviar = onEnviarResposta)
            }
        }
    }
}

@Composable
private fun CampoResposta(autor: String, onEnviar: (String) -> Unit) {
    var texto by rememberSaveable(autor) { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = texto,
            onValueChange = { texto = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Responder $autor…") },
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = { if (texto.isNotBlank()) onEnviar(texto.trim()) },
            enabled = texto.isNotBlank(),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, "Enviar resposta", modifier = Modifier.size(18.dp))
        }
    }
}
