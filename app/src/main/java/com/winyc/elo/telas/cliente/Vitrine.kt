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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.winyc.elo.R
import com.winyc.elo.backend.model.vitrine.ComentarioRS
import com.winyc.elo.backend.model.vitrine.PublicacaoFeedRS
import com.winyc.elo.backend.model.vitrine.PublicacaoImagemRS
import com.winyc.elo.backend.viewModel.CategoriaChip
import com.winyc.elo.backend.viewModel.ComentariosUi
import com.winyc.elo.backend.viewModel.VitrineViewModel
import com.winyc.elo.telas.componentes.AvatarPerfil
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/** Dispara o carregamento da próxima página quando faltam estas publicações para o fim. */
private const val GATILHO_PROXIMA_PAGINA = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitrineScreen(
    logado: Boolean,
    onAbrirPerfil: (String) -> Unit = {},
    onPrecisaLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
    vm: VitrineViewModel = viewModel(),
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val comentarios by vm.comentarios.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    val precisaCarregarMais by remember {
        derivedStateOf {
            val ultimoVisivel = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && ultimoVisivel >= total - 1 - GATILHO_PROXIMA_PAGINA
        }
    }
    androidx.compose.runtime.LaunchedEffect(precisaCarregarMais, estado.podeCarregarMais) {
        if (precisaCarregarMais && estado.podeCarregarMais) vm.carregarMais()
    }

    LazyColumn(
        state = listState,
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
                todos = stringResource(R.string.categoria_todos),
                categorias = estado.categorias,
                selecionada = estado.categoriaSelecionada,
                onSelecionar = vm::selecionarCategoria,
            )
        }

        items(estado.posts, key = { it.id }) { post ->
            TrabalhoCard(
                post = post,
                onAbrirPerfil = { post.profissionalNome?.let(onAbrirPerfil) },
                onCurtir = { if (logado) vm.alternarCurtida(post.id) else null },
                onComentar = { vm.abrirComentarios(post.id) },
            )
        }

        // Rodapé: carregando inicial, carregando mais, erro (com retry) ou vazio.
        item {
            when {
                estado.carregandoInicial || estado.carregandoMais -> RodapeCarregando()
                estado.erro != null -> RodapeErro(
                    mensagem = estado.erro!!,
                    onTentarNovamente = vm::carregarInicial
                )

                estado.posts.isEmpty() -> RodapeVazio()
            }
        }
    }

    comentarios?.let { estadoComentarios ->
        ComentariosSheet(
            estado = estadoComentarios,
            logado = logado,
            onFechar = vm::fecharComentarios,
            onEnviar = vm::enviarComentario,
            onCarregarMais = vm::carregarMaisComentarios,
            onPrecisaLogin = onPrecisaLogin,
        )
    }
}

@Composable
private fun Categorias(
    todos: String,
    categorias: List<CategoriaChip>,
    selecionada: Long?,
    onSelecionar: (Long?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            CategoriaChipUi(
                rotulo = todos,
                selecionado = selecionada == null
            ) { onSelecionar(null) }
        }
        items(categorias, key = { it.id }) { categoria ->
            CategoriaChipUi(
                rotulo = categoria.nome,
                selecionado = categoria.id == selecionada,
            ) { onSelecionar(categoria.id) }
        }
    }
}

@Composable
private fun CategoriaChipUi(rotulo: String, selecionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selecionado,
        onClick = onClick,
        label = {
            Text(
                text = rotulo,
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

@Composable
private fun TrabalhoCard(
    post: PublicacaoFeedRS,
    onAbrirPerfil: () -> Unit,
    onCurtir: () -> Unit,
    onComentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        FotoTrabalho(imagens = post.imagens, categoria = post.categoriaNome)

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Cabecalho(post, onAbrirPerfil = onAbrirPerfil)
            Text(
                text = post.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            RodapeCard(
                curtido = post.isCurtido,
                curtidas = post.quantidadeCurtidas,
                comentarios = post.quantidadeComentarios,
                onCurtir = onCurtir,
                onComentar = onComentar,
                onVerPerfil = onAbrirPerfil,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FotoTrabalho(imagens: List<PublicacaoImagemRS>, categoria: String?) {
    val ordenadas = remember(imagens) { imagens.sortedBy { it.ordem ?: Int.MAX_VALUE } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (ordenadas.isEmpty()) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(40.dp),
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { ordenadas.size })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pagina ->
                AsyncImage(
                    model = ordenadas[pagina].url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (ordenadas.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${ordenadas.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        }

        if (!categoria.isNullOrBlank()) {
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
}

@Composable
private fun Cabecalho(post: PublicacaoFeedRS, onAbrirPerfil: () -> Unit) {
    val nome = post.profissionalNome.orEmpty()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onAbrirPerfil),
    ) {
        AvatarPerfil(
            nome = nome,
            fotoUrl = post.profissionalFotoUrl,
            tamanho = 40.dp,
            fonte = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nome,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = tempoRelativo(post.publicadoEm),
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
private fun RodapeCard(
    curtido: Boolean,
    curtidas: Long,
    comentarios: Long,
    onCurtir: () -> Unit,
    onComentar: () -> Unit,
    onVerPerfil: () -> Unit,
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
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onVerPerfil)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun Contador(icone: ImageVector, valor: String, tint: Color, onClick: () -> Unit) {
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
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RodapeCarregando() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
    }
}

@Composable
private fun RodapeErro(mensagem: String, onTentarNovamente: () -> Unit) {
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
        Button(onClick = onTentarNovamente) { Text(stringResource(R.string.vitrine_tentar_novamente)) }
    }
}

@Composable
private fun RodapeVazio() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.vitrine_vazia),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComentariosSheet(
    estado: ComentariosUi,
    logado: Boolean,
    onEnviar: (String) -> Unit,
    onFechar: () -> Unit,
    onCarregarMais: () -> Unit,
    onPrecisaLogin: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val listState = rememberLazyListState()

    val precisaCarregarMais by remember {
        derivedStateOf {
            val ultimoVisivel = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && ultimoVisivel >= total - 1 - GATILHO_PROXIMA_PAGINA
        }
    }
    androidx.compose.runtime.LaunchedEffect(precisaCarregarMais, estado.podeCarregarMais) {
        if (precisaCarregarMais && estado.podeCarregarMais) onCarregarMais()
    }

    ModalBottomSheet(
        onDismissRequest = onFechar, sheetState = sheetState, sheetGesturesEnabled = false,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .imePadding()
                .navigationBarsPadding()
                .padding(vertical = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.vitrine_comentarios),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Box(modifier = Modifier.weight(1f)) {
                when {
                    estado.carregando -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }

                    estado.comentarios.isEmpty() && estado.erro != null -> MensagemCentral(estado.erro)

                    estado.comentarios.isEmpty() -> MensagemCentral(stringResource(R.string.vitrine_sem_comentarios))

                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(estado.comentarios, key = { it.id }) { comentario ->
                            ComentarioItem(comentario)
                        }
                        if (estado.carregandoMais) {
                            item { RodapeCarregando() }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            if (logado) {
                InputComentario(enviando = estado.enviando, onEnviar = onEnviar)
            } else {
                ConviteLogin(onPrecisaLogin = onPrecisaLogin)
            }
        }
    }
}

@Composable
private fun MensagemCentral(texto: String) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ComentarioItem(comentario: ComentarioRS) {
    val nome = comentario.usuarioNome.orEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        AvatarPerfil(
            nome = nome,
            fotoUrl = comentario.usuarioFotoUrl,
            tamanho = 36.dp,
            fonte = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nome,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tempoRelativo(comentario.comentadoEm),
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
private fun InputComentario(enviando: Boolean, onEnviar: (String) -> Unit) {
    var texto by rememberSaveable { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = { if (it.length <= 200) texto = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.vitrine_escreva_comentario)) },
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            enabled = !enviando,
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = {
                if (texto.isNotBlank()) {
                    onEnviar(texto.trim())
                    texto = ""
                }
            },
            enabled = texto.isNotBlank() && !enviando,
        ) {
            if (enviando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.vitrine_enviar_comentario),
                )
            }
        }
    }
}

@Composable
private fun ConviteLogin(onPrecisaLogin: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.vitrine_entre_para_comentar),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onPrecisaLogin) { Text(stringResource(R.string.deslogado_entrar)) }
    }
}

/** Converte um timestamp ISO-8601 do backend em rótulo curto (agora, 5min, 3h, 2d…). */
private fun tempoRelativo(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val dt = LocalDateTime.parse(iso)
        val minutos = ChronoUnit.MINUTES.between(dt, LocalDateTime.now())
        when {
            minutos < 1 -> "agora"
            minutos < 60 -> "${minutos}min"
            minutos < 1_440 -> "${minutos / 60}h"
            minutos < 10_080 -> "${minutos / 1_440}d"
            else -> "${minutos / 10_080}sem"
        }
    } catch (_: Exception) {
        ""
    }
}
