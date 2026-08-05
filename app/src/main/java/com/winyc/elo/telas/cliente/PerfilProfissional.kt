package com.winyc.elo.telas.cliente

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Reviews
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.winyc.elo.R
import com.winyc.elo.backend.model.endereco.EnderecoRS
import com.winyc.elo.backend.model.endereco.linhaEndereco
import com.winyc.elo.backend.model.estimativa.AvaliacaoRS
import com.winyc.elo.backend.model.estimativa.ProfissionalDetalhesRS
import com.winyc.elo.backend.model.estimativa.ResumoAvaliacoesRS
import com.winyc.elo.backend.model.estimativa.ServicoOferecidoRS
import com.winyc.elo.backend.model.estimativa.pontos
import com.winyc.elo.backend.model.imagem.EscopoImagem
import com.winyc.elo.backend.model.orcamento.DiaHorariosRS
import com.winyc.elo.backend.viewModel.ComentariosAvaliacaoUi
import com.winyc.elo.backend.viewModel.EnderecosUi
import com.winyc.elo.backend.viewModel.HorariosUi
import com.winyc.elo.backend.viewModel.OrcamentoViewModel
import com.winyc.elo.backend.viewModel.ProfissionalPerfilViewModel
import com.winyc.elo.telas.componentes.AvatarPerfil
import com.winyc.elo.telas.componentes.EstadoImagens
import com.winyc.elo.telas.componentes.GradeImagens
import com.winyc.elo.telas.componentes.rememberEstadoImagens
import com.winyc.elo.ui.theme.EloTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private const val MAX_IMAGENS_ORCAMENTO = 3

// Cores de apoio (fora do contexto coral/teal, iguais às usadas na home).
private val Verde = Color(0xFF12A15A)
private val Azul = Color(0xFF2F6BFF)
private val Roxo = Color(0xFF8B5CF6)

private fun primeiroNome(nome: String): String =
    nome.trim().split(" ").firstOrNull().orEmpty().ifBlank { nome }

/* ============================ Tela ============================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilProfissionalScreen(
    nome: String,
    logado: Boolean,
    onPrecisaLogin: () -> Unit,
    onVoltar: () -> Unit,
    proId: Long = -1L,
    servicoId: Long? = null,
    categoriaId: Long? = null,
    onIrParaInicio: () -> Unit = {},
    onVerOrcamentos: () -> Unit = {},
    onIrParaEnderecos: () -> Unit = {},
    modifier: Modifier = Modifier,
    vm: ProfissionalPerfilViewModel = viewModel(),
    userId: Long
) {
    val estadoVm by vm.estado.collectAsStateWithLifecycle()
    val dados = estadoVm.perfil

    LaunchedEffect(proId, servicoId, categoriaId) {
        when {
            proId <= 0 -> Unit
            servicoId != null && servicoId > 0 -> vm.carregarPorServico(proId, servicoId)
            categoriaId != null && categoriaId > 0 -> vm.carregarPorCategoria(proId, categoriaId)
        }
    }

    var verTodasAvaliacoes by rememberSaveable(proId) { mutableStateOf(false) }
    var escolhendoServico by rememberSaveable(proId) { mutableStateOf(false) }
    var servicoDetalhe by rememberSaveable(proId) { mutableStateOf<Long?>(null) }
    var servicoInfo by rememberSaveable(proId) { mutableStateOf<Long?>(null) }
    var sucesso by rememberSaveable(proId) { mutableStateOf(false) }
    var pedindoLogin by rememberSaveable(proId) { mutableStateOf(false) }
    var orcamentoBloqueado by rememberSaveable(proId) { mutableStateOf(false) }

    val servicos = dados?.servicosOferecidos.orEmpty()
    val nomeExibicao = dados?.profissional?.nome?.takeIf { it.isNotBlank() } ?: nome

    // A lista completa de comentários é buscada por categoria geral.
    val categoriaGeralId = remember(dados) {
        dados?.servicoSelecionado?.categoria?.categoriaGeralId
            ?: servicos.firstNotNullOfOrNull { it.categoria?.categoriaGeralId }
    }
    val comentariosUi by vm.comentarios.collectAsStateWithLifecycle()

    LaunchedEffect(verTodasAvaliacoes, proId, categoriaGeralId) {
        if (verTodasAvaliacoes && proId > 0 && categoriaGeralId != null) {
            vm.carregarComentarios(proId, categoriaGeralId)
        }
    }

    // Só quem está logado pode iniciar um orçamento; deslogado vê o convite.
    val iniciarOrcamento = {
        if (!logado) {
            pedindoLogin = true
        } else if ((dados?.profissional?.id.takeIf { it != null } ?: -1L) == userId) {
            orcamentoBloqueado = true
        } else {
            val selecionado = dados?.servicoSelecionado?.id
            if (selecionado != null) servicoDetalhe = selecionado else escolhendoServico = true
        }
    }

    BackHandler(enabled = sucesso) { onIrParaInicio() }
    BackHandler(enabled = !sucesso && servicoDetalhe != null) { servicoDetalhe = null }
    BackHandler(enabled = !sucesso && servicoDetalhe == null && escolhendoServico) {
        escolhendoServico = false
    }
    BackHandler(
        enabled = !sucesso && servicoDetalhe == null && !escolhendoServico && verTodasAvaliacoes,
    ) { verTodasAvaliacoes = false }

    when {
        sucesso -> SolicitacaoEnviadaScreen(
            nome = nomeExibicao,
            onInicio = onIrParaInicio,
            onOrcamentos = onVerOrcamentos,
            modifier = modifier,
        )

        dados == null && estadoVm.carregando -> CarregandoPerfil(onVoltar, modifier)

        dados == null -> ErroPerfil(
            mensagem = estadoVm.erro ?: "Não foi possível carregar o perfil.",
            onVoltar = onVoltar,
            onTentarNovamente = vm::tentarNovamente,
            modifier = modifier,
        )

        servicoDetalhe != null -> DetalhesServicoScreen(
            nome = nomeExibicao,
            servico = servicos.firstOrNull { it.id == servicoDetalhe },
            onVoltar = { servicoDetalhe = null },
            onTrocar = {
                servicoDetalhe = null
                escolhendoServico = true
            },
            onConfirmar = {
                servicoDetalhe = null
                escolhendoServico = false
                sucesso = true
            },
            onIrParaEnderecos = onIrParaEnderecos,
            modifier = modifier,
        )

        escolhendoServico -> EscolherServicoScreen(
            nome = nomeExibicao,
            servicos = servicos,
            onVoltar = { escolhendoServico = false },
            onAbrirInfo = { id -> servicoInfo = id },
            modifier = modifier,
        )

        verTodasAvaliacoes -> AvaliacoesScreen(
            nome = nomeExibicao,
            resumo = dados.resumoAvaliacoes,
            iniciais = dados.ultimasAvaliacoes,
            ui = comentariosUi,
            onTentarNovamente = {
                if (proId > 0 && categoriaGeralId != null) {
                    vm.carregarComentarios(proId, categoriaGeralId, forcar = true)
                }
            },
            onVoltar = { verTodasAvaliacoes = false },
            modifier = modifier,
        )

        else -> Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            val profissional = dados.profissional
            // Carrossel do topo: imagens dos serviços (não a foto de perfil).
            val imagensServicos = remember(servicos) {
                servicos.flatMap { it.imagens }
                    .sortedBy { it.ordem ?: Int.MAX_VALUE }
                    .mapNotNull { it.url?.takeIf { url -> url.isNotBlank() } }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 150.dp),
            ) {
                item {
                    Column {
                        Hero(
                            profissional = profissional,
                            nome = nomeExibicao,
                            imagens = imagensServicos,
                            onVoltar = onVoltar,
                        )
                        CardEstatisticas(
                            profissional = profissional,
                            modifier = Modifier
                                .offset(y = (-15).dp)
                                .padding(horizontal = 16.dp),
                        )
                    }
                }

                item {
                    SecaoSobre(
                        apresentacao = profissional?.apresentacao,
                        especialidades = profissional?.especialidades,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                item {
                    Spacer(Modifier.height(20.dp))
                    SecaoServicos(
                        servicos = servicos,
                        categoria = servicos.firstOrNull()?.categoria?.categoriaGeral,
                        onAbrirServico = { servicoInfo = it },
                        onVerTodos = { escolhendoServico = true },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    SecaoAvaliacoes(
                        resumo = dados.resumoAvaliacoes,
                        avaliacoes = dados.ultimasAvaliacoes,
                        onVerTodas = { verTodasAvaliacoes = true },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            BarraContratar(
                nome = nomeExibicao,
                precoMin = servicos.mapNotNull { it.valor }.minOrNull(),
                avaliacao = profissional?.avaliacao,
                quantidadeAvaliacoes = profissional?.quantidadeAvaliacoes ?: 0,
                onContratar = iniciarOrcamento,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    servicoInfo?.let { id ->
        servicos.firstOrNull { it.id == id }?.let { servico ->
            ServicoSheet(
                servico = servico,
                onFechar = { servicoInfo = null },
                onContratar = {
                    servicoInfo = null
                    if (logado) servicoDetalhe = id else pedindoLogin = true
                },
            )
        }
    }

    if (orcamentoBloqueado) {
        DialogNaoPodeReservar(
            onFechar = { orcamentoBloqueado = false },
        )
    }

    if (pedindoLogin) {
        DialogPrecisaLogin(
            onEntrar = {
                pedindoLogin = false
                onPrecisaLogin()
            },
            onCancelar = { pedindoLogin = false },
        )
    }
}

/* ---------------------------- Estados de carga ---------------------------- */

@Composable
private fun CarregandoPerfil(onVoltar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopoSimples(onVoltar = onVoltar)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ErroPerfil(
    mensagem: String,
    onVoltar: () -> Unit,
    onTentarNovamente: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopoSimples(onVoltar = onVoltar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                mensagem,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onTentarNovamente) { Text("Tentar novamente") }
        }
    }
}

@Composable
private fun TopoSimples(onVoltar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            "Voltar",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onVoltar)
                .padding(8.dp),
        )
    }
}

/* ---------------------------- Hero ---------------------------- */

@Composable
private fun Hero(
    profissional: ProfissionalDetalhesRS?,
    nome: String,
    imagens: List<String>,
    onVoltar: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (imagens.isEmpty()) {
                Icon(
                    Icons.Outlined.Image,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(44.dp),
                )
            } else {
                val pagerState = rememberPagerState(pageCount = { imagens.size })
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pagina ->
                    AsyncImage(
                        model = imagens[pagina],
                        contentDescription = nome,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                if (imagens.size > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(12.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${imagens.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        repeat(imagens.size) { indice ->
                            val ativo = indice == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .size(if (ativo) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = if (ativo) 1f else 0.5f)),
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.55f),
                    ),
                ),
        )

        BotaoCircular(
            icone = Icons.AutoMirrored.Filled.ArrowBack,
            descricao = "Voltar",
            onClick = onVoltar,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPerfil(
                    nome = nome,
                    fotoUrl = profissional?.fotoPerfil,
                    tamanho = 48.dp,
                    fonte = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = nome,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Verified,
                            "Verificado",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val avaliacao = profissional?.avaliacao
                        if (avaliacao != null && (profissional.quantidadeAvaliacoes ?: 0) > 0) {
                            Icon(
                                Icons.Filled.Star,
                                null,
                                tint = EloTheme.colors.avaliacao,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${formatarNota(avaliacao)} (${profissional.quantidadeAvaliacoes})",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                            )
                        } else {
                            Text(
                                "Novo na plataforma",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                            )
                        }
                        profissional?.distanciaKm?.let {
                            Spacer(Modifier.width(10.dp))
                            Icon(
                                Icons.Outlined.LocationOn,
                                null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                formatarKm(it),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotaoCircular(
    icone: ImageVector,
    descricao: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, descricao, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

/* ---------------------------- Estatísticas ---------------------------- */

@Composable
private fun CardEstatisticas(profissional: ProfissionalDetalhesRS?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            val avaliacao = profissional?.avaliacao
            Estatistica(
                Icons.Filled.Star,
                EloTheme.colors.avaliacao,
                if (avaliacao != null && (profissional.quantidadeAvaliacoes ?: 0) > 0) formatarNota(
                    avaliacao
                ) else "—",
                "Avaliação",
            )
            Estatistica(
                Icons.Outlined.ThumbUp,
                Azul,
                (profissional?.servicosConcluidos ?: 0).toString(),
                "Serviços",
            )
            Estatistica(
                Icons.Outlined.Schedule,
                Roxo,
                profissional?.tempoExperiencia?.let { "$it ${if (it == 1) "ano" else "anos"}" }
                    ?: "—",
                "Experiência",
            )
        }
    }
}

@Composable
private fun Estatistica(icone: ImageVector, cor: Color, valor: String, rotulo: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(cor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, null, tint = cor, modifier = Modifier.size(22.dp))
        }
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            rotulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ---------------------------- Sobre ---------------------------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecaoSobre(
    apresentacao: String?,
    especialidades: String?,
    modifier: Modifier = Modifier
) {
    val tags = remember(especialidades) {
        especialidades?.split(';', ',')?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
    }
    if (apresentacao.isNullOrBlank() && tags.isEmpty()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Sobre",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (!apresentacao.isNullOrBlank()) {
            Text(
                apresentacao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (tags.isNotEmpty()) {
            Text(
                "Especialidades:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { tag ->
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelLarge,
                        color = Verde,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Verde.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

/* ---------------------------- Serviços ---------------------------- */

@Composable
private fun SecaoServicos(
    servicos: List<ServicoOferecidoRS>,
    categoria: String?,
    onAbrirServico: (Long) -> Unit,
    onVerTodos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.servicos_oferecidos),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            if (!categoria.isNullOrBlank()) {
                Text(
                    text = categoria,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
        if (servicos.isEmpty()) {
            Text(
                "Este profissional ainda não cadastrou serviços.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        Text(
            stringResource(R.string.servico_descricao_valor_diferencial),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        servicos.take(3).forEachIndexed { indice, servico ->
            CardServico(
                numero = indice + 1,
                servico = servico,
                onClick = { onAbrirServico(servico.id) })
        }
        if (servicos.size > 3) {
            OutlinedButton(
                onClick = onVerTodos,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Outlined.Layers, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.todos_servicos))
                Spacer(Modifier.width(6.dp))
                Text("(${servicos.size})", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CardServico(numero: Int, servico: ServicoOferecidoRS, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    numero.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    servico.nome.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                servico.valor?.let {
                    Text(
                        stringResource(R.string.a_partir_de, formatarPreco(it)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CardServicoDisponivel(servico: ServicoOferecidoRS, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.LocalOffer,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    servico.nome.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                servico.descricao?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                servico.valor?.let {
                    Text(
                        stringResource(R.string.a_partir_de, formatarPreco(it)),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                DiferenciaisResumo(servico.pontos())
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiferenciaisResumo(diferenciais: List<String>) {
    if (diferenciais.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        diferenciais.take(2).forEach { ChipDiferencial(it) }
        if (diferenciais.size > 2) {
            Text(
                "+${diferenciais.size - 2}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChipDiferencial(texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(Verde.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Icon(Icons.Outlined.CheckCircle, null, tint = Verde, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(texto, style = MaterialTheme.typography.labelSmall, color = Verde)
    }
}

/* ---------------------------- Avaliações (dados reais) ---------------------------- */

@Composable
private fun SecaoAvaliacoes(
    resumo: ResumoAvaliacoesRS?,
    avaliacoes: List<AvaliacaoRS>,
    onVerTodas: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val quantidade = resumo?.quantidade ?: 0
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Avaliações",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            if (avaliacoes.size > 2 || quantidade > 2) {
                Text(
                    "Ver todas",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onVerTodas)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }

        if (quantidade == 0 && avaliacoes.isEmpty()) {
            AvaliacoesVazio()
            return@Column
        }

        if (resumo != null && quantidade > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatarNota(resumo.media ?: 0.0),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Estrelas((resumo.media ?: 0.0).toInt())
                    Text(
                        buildString {
                            append("$quantidade avaliações")
                            resumo.percentualPositivas?.let { append(" · ${it.toInt()}% positivas") }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        avaliacoes.take(2).forEach { CardAvaliacao(it) }
    }
}

@Composable
private fun AvaliacoesVazio() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Reviews,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Ainda não há avaliações. Seja o primeiro a avaliar depois de contratar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CardAvaliacao(avaliacao: AvaliacaoRS) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPerfil(
                    nome = avaliacao.avaliador.orEmpty(),
                    fotoUrl = avaliacao.fotoAvaliador,
                    tamanho = 36.dp,
                    fonte = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    avaliacao.avaliador.orEmpty().ifBlank { "Cliente" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Estrelas(avaliacao.nota ?: 0)
            }
            if (!avaliacao.comentario.isNullOrBlank()) {
                Text(
                    avaliacao.comentario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/* ---------------------------- Barra fixa: Contratar ---------------------------- */

@Composable
private fun BarraContratar(
    nome: String,
    precoMin: Double?,
    avaliacao: Double?,
    quantidadeAvaliacoes: Int,
    onContratar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        precoMin?.let { stringResource(R.string.a_partir_de, formatarPreco(it)) }
                            ?: "Valores a combinar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (avaliacao != null && quantidadeAvaliacoes > 0) {
                    Icon(
                        Icons.Filled.Star,
                        null,
                        tint = EloTheme.colors.avaliacao,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${formatarNota(avaliacao)} ($quantidadeAvaliacoes)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Button(
                onClick = onContratar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    "Contratar ${primeiroNome(nome)}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/* ---------------------------- Tela: todas as avaliações ---------------------------- */

/** Recorte por sentimento da nota, usado nos filtros e no selo do card. */
private enum class Sentimento(val rotulo: String) {
    POSITIVO("Positivo"),
    NEUTRO("Neutro"),
    NEGATIVO("Negativo"),
}

private fun sentimentoDe(nota: Int?): Sentimento = when {
    nota == null -> Sentimento.NEUTRO
    nota >= 4 -> Sentimento.POSITIVO
    nota == 3 -> Sentimento.NEUTRO
    else -> Sentimento.NEGATIVO
}

@Composable
private fun corDe(sentimento: Sentimento): Color = when (sentimento) {
    Sentimento.POSITIVO -> Verde
    Sentimento.NEUTRO -> EloTheme.colors.avaliacao
    Sentimento.NEGATIVO -> MaterialTheme.colorScheme.error
}

private fun iconeDe(sentimento: Sentimento): ImageVector = when (sentimento) {
    Sentimento.POSITIVO -> Icons.Outlined.SentimentSatisfiedAlt
    Sentimento.NEUTRO -> Icons.Outlined.SentimentNeutral
    Sentimento.NEGATIVO -> Icons.Outlined.SentimentDissatisfied
}

@Composable
private fun AvaliacoesScreen(
    nome: String,
    resumo: ResumoAvaliacoesRS?,
    iniciais: List<AvaliacaoRS>,
    ui: ComentariosAvaliacaoUi,
    onTentarNovamente: () -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Enquanto a lista completa não chega (ou se ela falhar), mostra as do perfil.
    // A data vem em ISO, então a ordenação por texto já deixa as mais recentes no topo.
    val avaliacoes = remember(ui.avaliacoes, iniciais) {
        ui.avaliacoes.ifEmpty { iniciais }
            .sortedWith(compareByDescending(nullsFirst<String>()) { it.dataCriacao })
    }
    var filtro by rememberSaveable { mutableStateOf<Sentimento?>(null) }

    val contagens = remember(avaliacoes) {
        avaliacoes.groupingBy { sentimentoDe(it.nota) }.eachCount()
    }
    val distribuicao = remember(avaliacoes) {
        avaliacoes.groupingBy { (it.nota ?: 0).coerceIn(1, 5) }.eachCount()
    }
    val filtradas = remember(avaliacoes, filtro) {
        filtro?.let { alvo -> avaliacoes.filter { sentimentoDe(it.nota) == alvo } } ?: avaliacoes
    }

    val media = resumo?.media ?: avaliacoes.mapNotNull { it.nota }.average().takeIf { !it.isNaN() }
    val quantidade = maxOf(resumo?.quantidade ?: 0, avaliacoes.size)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Voltar",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onVoltar)
                    .padding(8.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Avaliações de ${primeiroNome(nome)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (ui.carregando && avaliacoes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (quantidade > 0) {
                item {
                    CardResumoAvaliacoes(
                        media = media,
                        quantidade = quantidade,
                        percentualPositivas = resumo?.percentualPositivas,
                        distribuicao = distribuicao,
                    )
                }
            }

            if (ui.erro != null) {
                item { AvisoComentarios(mensagem = ui.erro, onTentarNovamente = onTentarNovamente) }
            }

            if (avaliacoes.isNotEmpty()) {
                item {
                    FiltrosAvaliacoes(
                        total = avaliacoes.size,
                        contagens = contagens,
                        selecionado = filtro,
                        onSelecionar = { filtro = it },
                    )
                }
            }

            when {
                avaliacoes.isEmpty() -> item { AvaliacoesVazio() }
                filtradas.isEmpty() -> item { FiltroSemResultado(filtro) }
                else -> items(filtradas, key = { it.id }) { CardAvaliacaoDetalhada(it) }
            }

            if (ui.carregando && avaliacoes.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardResumoAvaliacoes(
    media: Double?,
    quantidade: Int,
    percentualPositivas: Double?,
    distribuicao: Map<Int, Int>,
) {
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        media?.let { formatarNota(it) } ?: "—",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Estrelas((media ?: 0.0).toInt())
                    Text(
                        "$quantidade ${if (quantidade == 1) "avaliação" else "avaliações"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(20.dp))
                BarrasDistribuicao(
                    distribuicao = distribuicao,
                    modifier = Modifier.weight(1f),
                )
            }
            percentualPositivas?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.SentimentSatisfiedAlt,
                        null,
                        tint = Verde,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${it.toInt()}% das avaliações são positivas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Barras de 5 a 1 estrelas, proporcionais à nota mais frequente. */
@Composable
private fun BarrasDistribuicao(distribuicao: Map<Int, Int>, modifier: Modifier = Modifier) {
    val maior = distribuicao.values.maxOrNull() ?: 0
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        (5 downTo 1).forEach { nota ->
            val total = distribuicao[nota] ?: 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    nota.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(12.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    if (maior > 0 && total > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(total.toFloat() / maior)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(EloTheme.colors.avaliacao),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltrosAvaliacoes(
    total: Int,
    contagens: Map<Sentimento, Int>,
    selecionado: Sentimento?,
    onSelecionar: (Sentimento?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChipFiltro(
            rotulo = "Todas",
            quantidade = total,
            icone = null,
            cor = MaterialTheme.colorScheme.primary,
            selecionado = selecionado == null,
            onClick = { onSelecionar(null) },
        )
        Sentimento.entries.forEach { sentimento ->
            ChipFiltro(
                rotulo = sentimento.rotulo,
                quantidade = contagens[sentimento] ?: 0,
                icone = iconeDe(sentimento),
                cor = corDe(sentimento),
                selecionado = selecionado == sentimento,
                onClick = { onSelecionar(if (selecionado == sentimento) null else sentimento) },
            )
        }
    }
}

@Composable
private fun ChipFiltro(
    rotulo: String,
    quantidade: Int,
    icone: ImageVector?,
    cor: Color,
    selecionado: Boolean,
    onClick: () -> Unit,
) {
    val fundo = if (selecionado) cor else MaterialTheme.colorScheme.surface
    val conteudo = if (selecionado) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(fundo)
            .then(
                if (selecionado) Modifier
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icone?.let {
            Icon(it, null, tint = conteudo, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(rotulo, style = MaterialTheme.typography.labelLarge, color = conteudo)
        Spacer(Modifier.width(4.dp))
        Text(
            "($quantidade)",
            style = MaterialTheme.typography.labelMedium,
            color = conteudo.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun CardAvaliacaoDetalhada(avaliacao: AvaliacaoRS) {
    val sentimento = sentimentoDe(avaliacao.nota)
    val cor = corDe(sentimento)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPerfil(
                    nome = avaliacao.avaliador.orEmpty().ifBlank { "Cliente" },
                    fotoUrl = avaliacao.fotoAvaliador,
                    tamanho = 40.dp,
                    fonte = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        avaliacao.avaliador.orEmpty().ifBlank { "Cliente" },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    formatarDataAvaliacao(avaliacao.dataCriacao)?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Estrelas(avaliacao.nota ?: 0)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    sentimento.rotulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = cor,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(cor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            if (!avaliacao.comentario.isNullOrBlank()) {
                Text(
                    avaliacao.comentario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FiltroSemResultado(filtro: Sentimento?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Reviews,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Nenhuma avaliação ${filtro?.rotulo?.lowercase() ?: ""} por aqui.".trim(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Falha ao carregar a lista completa: as avaliações do perfil seguem visíveis. */
@Composable
private fun AvisoComentarios(mensagem: String, onTentarNovamente: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            mensagem,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onTentarNovamente) { Text("Tentar novamente") }
    }
}

/* ---------------------------- Fluxo: escolher serviço ---------------------------- */

@Composable
private fun EscolherServicoScreen(
    nome: String,
    servicos: List<ServicoOferecidoRS>,
    onVoltar: () -> Unit,
    onAbrirInfo: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CabecalhoFluxo(
            titulo = stringResource(R.string.qual_servico_voce_precisa),
            subtitulo = stringResource(R.string.selecione_descreva_livre),
            onVoltar = onVoltar,
        )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ProHeaderCard(nome) }
            item { RotuloSecao(stringResource(R.string.servicos_disponiveis)) }
            items(servicos, key = { it.id }) { servico ->
                CardServicoDisponivel(servico = servico, onClick = { onAbrirInfo(servico.id) })
            }
        }
    }
}

/* ---------------------------- Fluxo: detalhes do serviço ---------------------------- */

@Composable
private fun DetalhesServicoScreen(
    nome: String,
    servico: ServicoOferecidoRS?,
    onVoltar: () -> Unit,
    onTrocar: () -> Unit,
    onConfirmar: () -> Unit,
    onIrParaEnderecos: () -> Unit,
    modifier: Modifier = Modifier,
    vm: OrcamentoViewModel = viewModel(),
) {
    val context = LocalContext.current
    val servicoId = servico?.id ?: -1L
    val servicoValido = servicoId > 0

    val horariosUi by vm.horarios.collectAsStateWithLifecycle()
    val enderecosUi by vm.enderecos.collectAsStateWithLifecycle()
    val envioUi by vm.envio.collectAsStateWithLifecycle()

    LaunchedEffect(servicoId) { if (servicoValido) vm.iniciar(servicoId) }
    LaunchedEffect(envioUi.sucesso) { if (envioUi.sucesso) onConfirmar() }
    LaunchedEffect(envioUi.erro) {
        envioUi.erro?.let { mensagem ->
            Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
            vm.limparErroEnvio()
        }
    }

    var descricao by rememberSaveable(servico?.id) { mutableStateOf("") }
    var diaSelecionado by rememberSaveable(servico?.id) { mutableStateOf<String?>(null) }
    var horaSelecionada by rememberSaveable(servico?.id) { mutableStateOf<String?>(null) }
    var enderecoSelecionado by rememberSaveable(servico?.id) { mutableStateOf<Long?>(null) }
    val imagens = rememberEstadoImagens(maximo = MAX_IMAGENS_ORCAMENTO, chaveReinicio = servico?.id)

    // Assim que os endereços chegam, pré-seleciona o principal (ou o primeiro).
    LaunchedEffect(enderecosUi.enderecos) {
        if (enderecoSelecionado == null) {
            enderecoSelecionado = enderecosUi.enderecos.firstOrNull { it.stPrincipal == true }?.id
                ?: enderecosUi.enderecos.firstOrNull()?.id
        }
    }

    val dias = horariosUi.semana?.dias.orEmpty()
    LaunchedEffect(horariosUi.semana) {
        if (diaSelecionado != null && dias.none { it.data == diaSelecionado }) {
            diaSelecionado = null
            horaSelecionada = null
        }
    }

    val podeConfirmar = servicoValido &&
            descricao.isNotBlank() &&
            diaSelecionado != null &&
            horaSelecionada != null &&
            enderecoSelecionado != null &&
            !imagens.enviando

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CabecalhoFluxo(
            titulo = "Detalhes do serviço",
            subtitulo = "Escolha data, horário e endereço",
            onVoltar = onVoltar,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ProHeaderCard(nome) }
            item { ServicoSelecionadoBanner(servico = servico, onTrocar = onTrocar) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoRotulo(Icons.Outlined.Description, "Descreva o que precisa")
                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { if (it.length <= 100) descricao = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Descreva livremente o que você precisa…") },
                        supportingText = { Text("${descricao.length}/100") },
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
            item { SecaoImagens(imagens) }
            item {
                CalendarioHorarios(
                    servicoValido = servicoValido,
                    ui = horariosUi,
                    diaSelecionado = diaSelecionado,
                    horaSelecionada = horaSelecionada,
                    onSelecionarDia = { diaSelecionado = it; horaSelecionada = null },
                    onSelecionarHora = { horaSelecionada = it },
                    onSemanaAnterior = vm::semanaAnterior,
                    onProximaSemana = vm::proximaSemana,
                    onTentarNovamente = vm::tentarNovamenteHorarios,
                )
            }
            item {
                SecaoEndereco(
                    ui = enderecosUi,
                    selecionado = enderecoSelecionado,
                    onSelecionar = { enderecoSelecionado = it },
                    onTentarNovamente = vm::carregarEnderecos,
                    onCadastrarEndereco = onIrParaEnderecos,
                )
            }
            item { BannerContratacaoSegura() }
        }
        BarraConfirmar(
            habilitado = podeConfirmar,
            enviando = envioUi.enviando,
            onConfirmar = {
                val dia = diaSelecionado
                val hora = horaSelecionada
                if (dia != null && hora != null) {
                    vm.solicitar(
                        descricao = descricao,
                        dtPreferidoSolicitado = montarDataHora(dia, hora),
                        idEndereco = enderecoSelecionado,
                        chavesImagens = imagens.chaves,
                    )
                }
            },
        )
    }
}

/* ---------------------------- Calendário semanal de horários ---------------------------- */

@Composable
private fun CalendarioHorarios(
    servicoValido: Boolean,
    ui: HorariosUi,
    diaSelecionado: String?,
    horaSelecionada: String?,
    onSelecionarDia: (String) -> Unit,
    onSelecionarHora: (String) -> Unit,
    onSemanaAnterior: () -> Unit,
    onProximaSemana: () -> Unit,
    onTentarNovamente: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CampoRotulo(Icons.Outlined.CalendarToday, "Escolha data e horário")

        if (!servicoValido) {
            AvisoCalendario("Selecione um serviço específico para ver os horários disponíveis.")
            return@Column
        }

        val semana = ui.semana
        when {
            semana == null && ui.carregando -> CaixaCarregando()
            semana == null && ui.erro != null -> ErroCalendario(ui.erro, onTentarNovamente)
            semana == null -> AvisoCalendario("Nenhum horário disponível no momento.")
            else -> {
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
                            carregando = ui.carregando,
                            podeVoltar = ui.podeVoltarSemana && !ui.carregando,
                            onAnterior = onSemanaAnterior,
                            onProxima = onProximaSemana,
                        )
                        LinhaDias(
                            dias = semana.dias,
                            diaSelecionado = diaSelecionado,
                            onSelecionar = onSelecionarDia,
                        )
                        HorariosDoDia(
                            dia = semana.dias.firstOrNull { it.data == diaSelecionado },
                            horaSelecionada = horaSelecionada,
                            onSelecionar = onSelecionarHora,
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
            descricao = "Semana anterior",
            habilitado = podeVoltar,
            onClick = onAnterior,
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                rotulo,
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
            descricao = "Próxima semana",
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
private fun LinhaDias(
    dias: List<DiaHorariosRS>,
    diaSelecionado: String?,
    onSelecionar: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        dias.forEach { dia ->
            val data = dia.data ?: return@forEach
            val disponivel = dia.horariosDisponiveis.isNotEmpty()
            PilulaDia(
                abreviatura = abrevDiaSemana(data),
                diaMes = diaDoMes(data),
                selecionado = data == diaSelecionado,
                disponivel = disponivel,
                onClick = { if (disponivel) onSelecionar(data) },
                modifier = Modifier.weight(1f),
            )
        }
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
private fun HorariosDoDia(
    dia: DiaHorariosRS?,
    horaSelecionada: String?,
    onSelecionar: (String) -> Unit,
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    when {
        dia == null -> Text(
            "Selecione um dia para ver os horários.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        dia.horariosDisponiveis.isEmpty() -> Text(
            "Sem horários disponíveis neste dia.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        else -> FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            dia.horariosDisponiveis.forEach { hora ->
                ChipHorario(
                    texto = formatarHora(hora),
                    selecionado = hora == horaSelecionada,
                    onClick = { onSelecionar(hora) },
                )
            }
        }
    }
}

@Composable
private fun ChipHorario(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    val fundo =
        if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val corTexto =
        if (selecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = corTexto,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(fundo)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun AvisoCalendario(mensagem: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Schedule,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CaixaCarregando() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun ErroCalendario(mensagem: String, onTentarNovamente: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            "Tentar novamente",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onTentarNovamente)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

/* ---------------------------- Endereço (combobox) ---------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecaoEndereco(
    ui: EnderecosUi,
    selecionado: Long?,
    onSelecionar: (Long) -> Unit,
    onTentarNovamente: () -> Unit,
    onCadastrarEndereco: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CampoRotulo(Icons.Outlined.LocationOn, "Onde será o serviço?")
        when {
            ui.enderecos.isEmpty() && ui.carregando -> CaixaCarregando()
            ui.enderecos.isEmpty() && ui.erro != null -> ErroCalendario(ui.erro, onTentarNovamente)
            ui.enderecos.isEmpty() -> CadastrarEnderecoVazio(onCadastrarEndereco)

            else -> {
                var expandido by remember { mutableStateOf(false) }
                val enderecoAtual = ui.enderecos.firstOrNull { it.id == selecionado }
                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = { expandido = it },
                ) {
                    OutlinedTextField(
                        value = enderecoAtual?.let { rotuloEndereco(it) }
                            ?: "Selecione um endereço",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false },
                    ) {
                        ui.enderecos.forEach { endereco ->
                            DropdownMenuItem(
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            endereco.nmApelido?.takeIf { it.isNotBlank() }
                                                ?: "Endereço",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        endereco.linhaEndereco().takeIf { it.isNotBlank() }?.let {
                                            Text(
                                                it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.LocationOn,
                                        null,
                                        tint = if (endereco.id == selecionado) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                onClick = {
                                    onSelecionar(endereco.id)
                                    expandido = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CadastrarEnderecoVazio(onCadastrarEndereco: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.LocationOn,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Você precisa de um endereço cadastrado para solicitar o serviço.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = onCadastrarEndereco,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cadastrar endereço", style = MaterialTheme.typography.titleSmall)
        }
    }
}

private fun rotuloEndereco(endereco: EnderecoRS): String {
    val apelido = endereco.nmApelido?.takeIf { it.isNotBlank() }
    val linha = endereco.linhaEndereco().takeIf { it.isNotBlank() }
    return listOfNotNull(apelido, linha).joinToString(" • ").ifBlank { "Endereço" }
}

@Composable
private fun ServicoSelecionadoBanner(servico: ServicoOferecidoRS?, onTrocar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "SERVIÇO SELECIONADO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                servico?.nome ?: "Serviço",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            "Trocar",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onTrocar)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun SecaoImagens(imagens: EstadoImagens) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CampoRotulo(Icons.Outlined.Image, "Adicionar imagens")
            Spacer(Modifier.weight(1f))
            Text(
                "${imagens.itens.size}/${imagens.maximo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GradeImagens(estado = imagens, escopo = EscopoImagem.ORCAMENTO, altura = 96.dp)
        Text(
            "Envie fotos do local ou do problema para ajudar o profissional a entender melhor o serviço.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BannerContratacaoSegura() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Verde.copy(alpha = 0.12f))
            .padding(14.dp),
    ) {
        Icon(Icons.Outlined.Shield, null, tint = Verde, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "Contratação segura",
                style = MaterialTheme.typography.titleSmall,
                color = Verde,
            )
            Text(
                "Seus dados estão protegidos. O pagamento deve ser tratado exclusivamente " +
                        "entre cliente e profissional e só poderá ser realizado após a conclusão " +
                        "do serviço acordado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BarraConfirmar(habilitado: Boolean, enviando: Boolean, onConfirmar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = onConfirmar,
                enabled = habilitado && !enviando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (enviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Solicitar orçamento", style = MaterialTheme.typography.titleMedium)
                }
            }
            Text(
                "Você não será cobrado agora",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* ---------------------------- Peças do fluxo ---------------------------- */

@Composable
private fun CabecalhoFluxo(titulo: String, subtitulo: String, onVoltar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            "Voltar",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onVoltar)
                .padding(8.dp),
        )
        Spacer(Modifier.width(4.dp))
        Column {
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProHeaderCard(nome: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarPerfil(
                nome = nome,
                fotoUrl = null,
                tamanho = 48.dp,
                fonte = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        nome,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.Verified,
                        "Verificado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Icon(
                Icons.Outlined.WorkspacePremium,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CampoRotulo(icone: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icone,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            texto,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RotuloSecao(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}


/* ---------------------------- Detalhes rápidos do serviço (bottom sheet) ---------------------------- */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ServicoSheet(
    servico: ServicoOferecidoRS,
    onFechar: () -> Unit,
    onContratar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val pontos = servico.pontos()

    ModalBottomSheet(onDismissRequest = onFechar, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.LocalOffer,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        servico.nome.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    servico.valor?.let {
                        Text(
                            stringResource(R.string.a_partir_de, formatarPreco(it)),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    "Recolher",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { recolherBottomModal(scope, sheetState, onFechar) }
                        .padding(4.dp),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            servico.descricao?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            servico.valor?.let { valor ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.a_partir),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "R$ " + formatarPreco(valor),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (pontos.isNotEmpty()) {
                RotuloSecao("PONTOS PRINCIPAIS")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pontos.forEach { ChipDiferencial(it) }
                }
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { recolherBottomModal(scope, sheetState, onContratar) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Contratar este serviço", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}


@Composable
private fun DialogNaoPodeReservar(onFechar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onFechar,
        icon = {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.orcar_login_acao_bloqueada)) },
        text = { Text(stringResource(R.string.orcar_login_usuario_sendo_prof)) },
        confirmButton = {
            Button(onClick = onFechar) { Text(stringResource(R.string.fechar)) }
        }
    )
}


/* ---------------------------- Convite login ---------------------------- */

@Composable
private fun DialogPrecisaLogin(onEntrar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.orcar_login_titulo)) },
        text = { Text(stringResource(R.string.orcar_login_texto)) },
        confirmButton = {
            Button(onClick = onEntrar) { Text(stringResource(R.string.orcar_login_entrar)) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.orcar_login_agora_nao)) }
        },
    )
}

/* ---------------------------- Solicitação enviada (sucesso) ---------------------------- */

@Composable
private fun SolicitacaoEnviadaScreen(
    nome: String,
    onInicio: () -> Unit,
    onOrcamentos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Check,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Solicitação enviada!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${primeiroNome(nome)} vai analisar seu pedido e responder com um orçamento. " +
                        "Você será avisado assim que houver retorno.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Verde.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Schedule, null, tint = Verde, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Resposta em até 24h",
                    style = MaterialTheme.typography.labelMedium,
                    color = Verde,
                )
            }
        }

        Button(
            onClick = onOrcamentos,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.AutoMirrored.Outlined.ReceiptLong, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ver meus orçamentos", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onInicio,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Outlined.Home, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Voltar ao início", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun Estrelas(nota: Int) {
    Row {
        repeat(5) { indice ->
            Icon(
                Icons.Filled.Star,
                null,
                tint = if (indice < nota) EloTheme.colors.avaliacao
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

/* ---------------------------- Formatação ---------------------------- */

private fun formatarPreco(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toInt().toString() else "%.2f".format(valor).replace('.', ',')

private fun formatarKm(km: Double): String {
    val texto = if (km % 1.0 == 0.0) km.toInt().toString() else "%.1f".format(km)
    return "${texto.replace('.', ',')} km"
}

private fun formatarNota(nota: Double): String =
    if (nota % 1.0 == 0.0) nota.toInt().toString() else "%.1f".format(nota).replace('.', ',')

/* ---------------------------- Datas e horários ---------------------------- */

private val DIAS_SEMANA_ABREV = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
private val MESES_ABREV =
    listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

private fun montarDataHora(dataIso: String, horaIso: String): String =
    runCatching {
        LocalDateTime.of(LocalDate.parse(dataIso), LocalTime.parse(horaIso)).toString()
    }.getOrDefault("${dataIso}T$horaIso")

/** `2026-07-31T01:29:45.282` (ou só a data) vira `31/07/2026`. */
private fun formatarDataAvaliacao(dataIso: String?): String? {
    if (dataIso.isNullOrBlank()) return null
    val data = runCatching { LocalDateTime.parse(dataIso).toLocalDate() }
        .recoverCatching { LocalDate.parse(dataIso.take(10)) }
        .getOrNull() ?: return null
    return "%02d/%02d/%d".format(data.dayOfMonth, data.monthValue, data.year)
}

private fun abrevDiaSemana(dataIso: String): String =
    runCatching { DIAS_SEMANA_ABREV[LocalDate.parse(dataIso).dayOfWeek.value - 1] }.getOrDefault("")

private fun diaDoMes(dataIso: String): String =
    runCatching { LocalDate.parse(dataIso).dayOfMonth.toString() }.getOrDefault("")

private fun formatarHora(horaIso: String): String =
    runCatching {
        val hora = LocalTime.parse(horaIso)
        "%02d:%02d".format(hora.hour, hora.minute)
    }.getOrDefault(horaIso)

private fun rotuloSemana(inicioIso: String?, fimIso: String?): String {
    val inicio = inicioIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val fim = fimIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    if (inicio == null || fim == null) return "Esta semana"
    return if (inicio.monthValue == fim.monthValue) {
        "${inicio.dayOfMonth} – ${fim.dayOfMonth} de ${MESES_ABREV[fim.monthValue - 1]}"
    } else {
        "${inicio.dayOfMonth} ${MESES_ABREV[inicio.monthValue - 1]} – " +
                "${fim.dayOfMonth} ${MESES_ABREV[fim.monthValue - 1]}"
    }
}
