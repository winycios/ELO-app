package com.winyc.elo.telas.profissional

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalOffer
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.winyc.elo.backend.model.vitrine.ComentarioRS
import com.winyc.elo.backend.model.vitrine.PublicacaoFeedRS
import com.winyc.elo.backend.model.vitrine.PublicacaoImagemRQ
import com.winyc.elo.backend.model.vitrine.PublicacaoImagemRS
import com.winyc.elo.backend.viewModel.CategoriaViewModel
import com.winyc.elo.backend.viewModel.ComentariosPubUi
import com.winyc.elo.backend.viewModel.ProfissionalViewModel
import com.winyc.elo.backend.viewModel.PublicacaoViewModel
import com.winyc.elo.telas.componentes.AvatarPerfil
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val GATILHO_PROXIMA_PAGINA = 5

private data class ServicoOpcao(val idCategoriaEspecifica: Long, val nome: String)

/** Tela "Publicar": compõe um post e lista as publicações do profissional. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarScreen(
    modifier: Modifier = Modifier,
    servicoVm: ProfissionalViewModel = viewModel(),
    categoriaVm: CategoriaViewModel = viewModel(),
    publicacaoVm: PublicacaoViewModel = viewModel(),
) {
    val servicoEstado by servicoVm.estado.collectAsStateWithLifecycle()
    val categoriaEstado by categoriaVm.estado.collectAsStateWithLifecycle()
    val estado by publicacaoVm.estado.collectAsStateWithLifecycle()
    val comentarios by publicacaoVm.comentarios.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { servicoVm.carregar() }

    // Nomes dos serviços cadastrados pelo profissional (resolvidos via categorias).
    val nomeEspecifica = remember(categoriaEstado.categoriasRaw) {
        categoriaEstado.categoriasRaw.flatMap { it.categoriaEspecificaList }
            .associate { it.id to it.nmCategoria }
    }
    val servicos = remember(servicoEstado.servicos, nomeEspecifica) {
        servicoEstado.servicos
            .mapNotNull { s ->
                val id = s.categoria?.idCategoriaEspecifica ?: return@mapNotNull null
                val nome = nomeEspecifica[id] ?: return@mapNotNull null
                ServicoOpcao(id, nome)
            }
            .distinctBy { it.idCategoriaEspecifica }
    }

    val listState = rememberLazyListState()
    val precisaCarregarMais by remember {
        derivedStateOf {
            val ultimoVisivel = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && ultimoVisivel >= total - 1 - GATILHO_PROXIMA_PAGINA
        }
    }
    LaunchedEffect(precisaCarregarMais, estado.podeCarregarMais) {
        if (precisaCarregarMais && estado.podeCarregarMais) publicacaoVm.carregarMais()
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
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

        item {
            when {
                // Serviços existem mas ainda resolvendo os nomes das categorias.
                servicoEstado.servicos.isNotEmpty() && servicos.isEmpty() -> ComporCarregando()
                servicoEstado.carregando && servicoEstado.servicos.isEmpty() -> ComporCarregando()
                // Sem serviço cadastrado: não é possível publicar.
                servicos.isEmpty() -> SemServicosPublicar()
                else -> ComporPublicacao(
                    servicos = servicos,
                    publicando = estado.publicando,
                    onPublicar = { idCategoria, descricao, imagens, onSucesso ->
                        publicacaoVm.publicar(
                            idCategoria,
                            descricao,
                            imagens
                        ) { ok -> if (ok) onSucesso() }
                    },
                )
            }
        }

        item {
            Text(
                text = "Minhas publicações",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        items(estado.publicacoes, key = { it.id }) { pub ->
            PublicacaoCard(
                pub = pub,
                onComentarios = { publicacaoVm.abrirComentarios(pub.id) },
                onExcluir = { publicacaoVm.excluir(pub.id) },
            )
        }

        item {
            when {
                estado.carregandoInicial || estado.carregandoMais -> RodapeCarregando()
                estado.erro != null -> RodapeErro(
                    mensagem = estado.erro!!,
                    onTentarNovamente = publicacaoVm::carregarInicial
                )

                estado.publicacoes.isEmpty() -> RodapeVazio()
            }
        }
    }

    comentarios?.let { estadoComentarios ->
        ComentariosPublicacaoSheet(
            estado = estadoComentarios,
            onFechar = publicacaoVm::fecharComentarios,
            onEnviar = { texto -> publicacaoVm.enviarComentario(texto) },
            onResponder = { paiId, texto -> publicacaoVm.enviarComentario(texto, paiId) },
            onAlternarResponder = publicacaoVm::alternarResponder,
            onCarregarMais = publicacaoVm::carregarMaisComentarios,
        )
    }
}

@Composable
private fun ComporCarregando() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator(modifier = Modifier.size(28.dp)) }
    }
}

@Composable
private fun SemServicosPublicar() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.LocalOffer,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "Nenhum serviço cadastrado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Cadastre um serviço em Perfil › Meus serviços para poder publicar na Vitrine.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val MAX_DESCRICAO = 200

@Composable
private fun ComporPublicacao(
    servicos: List<ServicoOpcao>,
    publicando: Boolean,
    onPublicar: (idCategoriaEspecifica: Long, descricao: String, imagens: List<PublicacaoImagemRQ>, onSucesso: () -> Unit) -> Unit,
) {
    var servico by rememberSaveable(servicos) { mutableStateOf(servicos.first().idCategoriaEspecifica) }
    var descricao by rememberSaveable { mutableStateOf("") }
    val imagensUrl = remember { mutableStateListOf("") }

    val selecionado =
        servicos.firstOrNull { it.idCategoriaEspecifica == servico } ?: servicos.first()

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
                Icon(
                    Icons.Outlined.LocalOffer,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Sobre qual serviço?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(servicos, key = { it.idCategoriaEspecifica }) { s ->
                    val sel = s.idCategoriaEspecifica == servico
                    FilterChip(
                        selected = sel,
                        onClick = { servico = s.idCategoriaEspecifica },
                        label = {
                            Text(
                                s.nome,
                                fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal
                            )
                        },
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

            Text(
                "Imagens (URL)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            imagensUrl.forEachIndexed { i, url ->
                ImagemUrlCampo(
                    url = url,
                    onUrlChange = { imagensUrl[i] = it },
                    onRemover = if (imagensUrl.size > 1) {
                        { imagensUrl.removeAt(i) }
                    } else null,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { imagensUrl.add("") }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Add,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Adicionar imagem",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TextField(
                value = descricao,
                onValueChange = { if (it.length <= MAX_DESCRICAO) descricao = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                placeholder = { Text("Conte sobre esse trabalho…") },
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("${descricao.length}/$MAX_DESCRICAO") },
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
                Icon(
                    Icons.Outlined.LocalOffer,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    buildAnnotatedString {
                        append("Será publicado em ")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(selecionado.nome)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Button(
                onClick = {
                    val imagens = imagensUrl
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .mapIndexed { i, u -> PublicacaoImagemRQ(urlImagem = u, nrOrdem = i) }
                    onPublicar(selecionado.idCategoriaEspecifica, descricao, imagens) {
                        descricao = ""
                        imagensUrl.clear()
                        imagensUrl.add("")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = descricao.isNotBlank() && !publicando,
            ) {
                if (publicando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Publicar na Vitrine")
                }
            }
        }
    }
}

@Composable
private fun ImagemUrlCampo(url: String, onUrlChange: (String) -> Unit, onRemover: (() -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (url.isBlank()) {
                Icon(
                    Icons.Outlined.Image,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        TextField(
            value = url,
            onValueChange = onUrlChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("https://…") },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        if (onRemover != null) {
            IconButton(onClick = onRemover) {
                Icon(
                    Icons.Outlined.Close,
                    "Remover imagem",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublicacaoCard(
    pub: PublicacaoFeedRS,
    onComentarios: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        FotoPublicacao(imagens = pub.imagens, categoria = pub.categoriaNome)

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                pub.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                MetricaIcone(
                    Icons.Outlined.FavoriteBorder,
                    pub.quantidadeCurtidas.toString(),
                    onClick = null
                )
                Spacer(Modifier.width(16.dp))
                MetricaIcone(
                    Icons.Outlined.ChatBubbleOutline,
                    pub.quantidadeComentarios.toString(),
                    onClick = onComentarios
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    tempoRelativo(pub.publicadoEm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onExcluir) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        "Excluir publicação",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FotoPublicacao(imagens: List<PublicacaoImagemRS>, categoria: String?) {
    val ordenadas = remember(imagens) { imagens.sortedBy { it.ordem ?: Int.MAX_VALUE } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (ordenadas.isEmpty()) {
            Icon(
                Icons.Outlined.Image,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(40.dp)
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
                        "${pagerState.currentPage + 1}/${ordenadas.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }

        if (!categoria.isNullOrBlank()) {
            Text(
                text = categoria,
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
    }
}

@Composable
private fun MetricaIcone(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    onClick: (() -> Unit)?
) {
    val base = Modifier
        .clip(RoundedCornerShape(8.dp))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(4.dp)
    Row(modifier = base, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icone,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RodapeCarregando() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
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
            mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onTentarNovamente) { Text("Tentar novamente") }
    }
}

@Composable
private fun RodapeVazio() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Você ainda não publicou nada. Compartilhe seu primeiro trabalho!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/* ---------------------------- Comentários ---------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComentariosPublicacaoSheet(
    estado: ComentariosPubUi,
    onFechar: () -> Unit,
    onEnviar: (String) -> Unit,
    onResponder: (paiId: Long, texto: String) -> Unit,
    onAlternarResponder: (Long?) -> Unit,
    onCarregarMais: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val raizes =
        remember(estado.comentarios) { estado.comentarios.filter { it.comentarioPaiId == null } }
    val respostasPorPai = remember(estado.comentarios) {
        estado.comentarios.filter { it.comentarioPaiId != null }.groupBy { it.comentarioPaiId }
    }

    val listState = rememberLazyListState()
    val precisaCarregarMais by remember {
        derivedStateOf {
            val ultimoVisivel = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && ultimoVisivel >= total - 1 - GATILHO_PROXIMA_PAGINA
        }
    }
    LaunchedEffect(precisaCarregarMais, estado.podeCarregarMais) {
        if (precisaCarregarMais && estado.podeCarregarMais) onCarregarMais()
    }

    ModalBottomSheet(onDismissRequest = onFechar, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .imePadding()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Comentários (${estado.comentarios.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onFechar,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        "Fechar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.size(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Box(modifier = Modifier.weight(1f)) {
                when {
                    estado.carregando -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }

                    estado.comentarios.isEmpty() && estado.erro != null -> MensagemCentral(estado.erro)
                    estado.comentarios.isEmpty() -> MensagemCentral("Nenhum comentário ainda.")
                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(raizes, key = { it.id }) { raiz ->
                            ComentarioRaiz(
                                comentario = raiz,
                                respostas = respostasPorPai[raiz.id].orEmpty(),
                                respondendo = estado.respondendoId == raiz.id,
                                enviando = estado.enviando,
                                onResponder = { onAlternarResponder(raiz.id) },
                                onEnviarResposta = { texto -> onResponder(raiz.id, texto) },
                            )
                        }
                        if (estado.carregandoMais) item { RodapeCarregando() }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            InputComentario(enviando = estado.enviando, onEnviar = onEnviar)
        }
    }
}

@Composable
private fun MensagemCentral(texto: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComentarioRaiz(
    comentario: ComentarioRS,
    respostas: List<ComentarioRS>,
    respondendo: Boolean,
    enviando: Boolean,
    onResponder: () -> Unit,
    onEnviarResposta: (String) -> Unit,
) {
    Row {
        AvatarPerfil(
            nome = comentario.usuarioNome.orEmpty(),
            fotoUrl = comentario.usuarioFotoUrl,
            tamanho = 36.dp,
            fonte = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            BalaoComentario(comentario)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onResponder)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Reply,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (respondendo) "Cancelar" else "Responder",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            respostas.forEach { resposta ->
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    AvatarPerfil(
                        nome = resposta.usuarioNome.orEmpty(),
                        fotoUrl = resposta.usuarioFotoUrl,
                        tamanho = 28.dp,
                        fonte = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) { BalaoComentario(resposta) }
                }
            }

            if (respondendo) {
                CampoResposta(
                    autor = comentario.usuarioNome.orEmpty(),
                    enviando = enviando,
                    onEnviar = onEnviarResposta,
                )
            }
        }
    }
}

@Composable
private fun BalaoComentario(comentario: ComentarioRS) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                comentario.usuarioNome.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                tempoRelativo(comentario.comentadoEm),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.size(2.dp))
        Text(
            comentario.texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CampoResposta(autor: String, enviando: Boolean, onEnviar: (String) -> Unit) {
    var texto by rememberSaveable(autor) { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = texto,
            onValueChange = { if (it.length <= MAX_DESCRICAO) texto = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Responder $autor…") },
            shape = CircleShape,
            enabled = !enviando,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = {
                if (texto.isNotBlank()) {
                    onEnviar(texto.trim()); texto = ""
                }
            },
            enabled = texto.isNotBlank() && !enviando,
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, "Enviar resposta", modifier = Modifier.size(18.dp))
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
        TextField(
            value = texto,
            onValueChange = { if (it.length <= MAX_DESCRICAO) texto = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escreva um comentário…") },
            shape = CircleShape,
            enabled = !enviando,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = {
                if (texto.isNotBlank()) {
                    onEnviar(texto.trim()); texto = ""
                }
            },
            enabled = texto.isNotBlank() && !enviando,
        ) {
            if (enviando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    "Enviar comentário",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
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
