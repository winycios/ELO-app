package com.winyc.elo.telas.cliente

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winyc.elo.R

/** Um post de trabalho exibido no feed da vitrine. */
private data class TrabalhoPost(
    val autor: String,
    val categoria: String,
    val tempo: String,
    val descricao: String,
    val curtidas: Int,
    val verificado: Boolean,
)

/** Um comentário de um post. */
private data class Comentario(
    val autor: String,
    val texto: String,
    val tempo: String,
)

private val CATEGORIAS = listOf(
    "Eletricista", "Diarista", "Encanador", "Jardineiro",
    "Pintor", "Montador", "Pedreiro", "Marceneiro",
)

private val POSTS = listOf(
    TrabalhoPost(
        autor = "Carlos Silva",
        categoria = "Eletricista",
        tempo = "3h",
        descricao = "Mais uma instalação elétrica completa finalizada! Quadro de " +
                "distribuição novo com disjuntores individuais para cada circuito. " +
                "Segurança em primeiro lugar! ⚡",
        curtidas = 87,
        verificado = true,
    ),
    TrabalhoPost(
        autor = "Ana Souza",
        categoria = "Pintor",
        tempo = "5h",
        descricao = "Sala de estar renovada com um tom de azul suave. Acabamento " +
                "impecável e sem respingos! 🎨",
        curtidas = 124,
        verificado = true,
    ),
    TrabalhoPost(
        autor = "Roberto Lima",
        categoria = "Encanador",
        tempo = "1d",
        descricao = "Troca completa do encanamento de um apartamento antigo. " +
                "Zero vazamentos e garantia total no serviço.",
        curtidas = 56,
        verificado = false,
    ),
)

private val COMENTARIOS_DEMO = listOf(
    Comentario("Marina Alves", "Trabalho impecável como sempre! 👏", "1d"),
    Comentario("João Pereira", "Ficou ótimo! Como faço pra pedir um orçamento?", "2d"),
    Comentario("Beatriz Rocha", "Recomendo demais, super pontual.", "3d"),
)

@Composable
fun VitrineScreen(modifier: Modifier = Modifier) {
    val rotuloTodos = stringResource(R.string.categoria_todos)
    var categoriaSelecionada by rememberSaveable { mutableStateOf(rotuloTodos) }

    val postsVisiveis = remember(categoriaSelecionada) {
        if (categoriaSelecionada == rotuloTodos) POSTS
        else POSTS.filter { it.categoria == categoriaSelecionada }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.vitrine),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.trabalhos_recentes_profissional),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Categorias(
                todos = rotuloTodos,
                selecionada = categoriaSelecionada,
                onSelecionar = { categoriaSelecionada = it },
            )
        }

        items(postsVisiveis) { post ->
            TrabalhoCard(post)
        }
    }
}

@Composable
private fun Categorias(
    todos: String,
    selecionada: String,
    onSelecionar: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf(todos) + CATEGORIAS) { categoria ->
            val selecionado = categoria == selecionada
            FilterChip(
                selected = selecionado,
                onClick = { onSelecionar(categoria) },
                label = {
                    Text(
                        text = categoria,
                        fontWeight = if (selecionado) FontWeight.Medium else FontWeight.Normal,
                    )
                },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selecionado,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrabalhoCard(post: TrabalhoPost, modifier: Modifier = Modifier) {

    var curtido by rememberSaveable(post.autor) { mutableStateOf(false) }
    var curtidas by rememberSaveable(post.autor) { mutableIntStateOf(post.curtidas) }
    var comentando by rememberSaveable(post.autor) { mutableStateOf(false) }
    val comentarios = remember(post.autor) { mutableStateListOf<Comentario>().apply { addAll(COMENTARIOS_DEMO) } }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        FotoTrabalho(categoria = post.categoria)

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Cabecalho(post)
            Text(
                text = post.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            RodapeCard(
                curtido = curtido,
                curtidas = curtidas,
                comentarios = comentarios.size,
                onCurtir = {
                    curtido = !curtido
                    curtidas += if (curtido) 1 else -1
                },
                onComentar = { comentando = true },
            )
        }
    }

    if (comentando) {
        ComentariosSheet(
            comentarios = comentarios,

            onFechar = { comentando = false },
            onEnviar = { texto ->
                comentarios.add(0, Comentario("Você", texto, "agora"))
            },
        )
    }
}

@Composable
private fun FotoTrabalho(categoria: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(40.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = categoria,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun Cabecalho(post: TrabalhoPost) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(post.autor)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.autor,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (post.verificado) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verificado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                text = post.tempo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Avatar(nome: String, tamanho: androidx.compose.ui.unit.Dp = 40.dp) {
    val iniciais = nome.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = iniciais,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun RodapeCard(
    curtido: Boolean,
    curtidas: Int,
    comentarios: Int,
    onCurtir: () -> Unit,
    onComentar: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Contador(
            icone = if (curtido) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            valor = curtidas.toString(),
            tint = if (curtido) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onCurtir,
        )
        Spacer(Modifier.width(20.dp))
        Contador(
            icone = Icons.Outlined.ChatBubbleOutline,
            valor = comentarios.toString(),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onComentar,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.ver_perfil),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun Contador(
    icone: ImageVector,
    valor: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComentariosSheet(
    comentarios: List<Comentario>,
    onEnviar: (String) -> Unit,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .imePadding()
                .navigationBarsPadding(),
        ) {
            Text(
                text = "Comentários",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(comentarios) { comentario ->
                    ComentarioItem(comentario)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            InputComentario(onEnviar = onEnviar)
        }
    }
}

@Composable
private fun ComentarioItem(comentario: Comentario) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Avatar(comentario.autor, tamanho = 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comentario.autor,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = comentario.tempo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(2.dp))
            Text(
                text = comentario.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun InputComentario(onEnviar: (String) -> Unit) {
    var texto by rememberSaveable { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escreva um comentário…") },
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = {
                if (texto.isNotBlank()) {
                    onEnviar(texto.trim())
                    texto = ""
                }
            },
            enabled = texto.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Enviar comentário",
            )
        }
    }
}
