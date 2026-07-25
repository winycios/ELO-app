package com.winyc.elo.telas.cliente

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Carpenter
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.DesignServices
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Elderly
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Foundation
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PestControl
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Plumbing
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SolarPower
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.winyc.elo.backend.model.endereco.EnderecoRS
import com.winyc.elo.backend.model.endereco.linhaEndereco
import com.winyc.elo.backend.model.search.OrdenacaoBusca
import com.winyc.elo.backend.model.search.ProfissionalBuscaRS
import com.winyc.elo.backend.model.search.servicoDeInteresse
import com.winyc.elo.backend.security.PerfilSessao
import com.winyc.elo.backend.viewModel.BuscaViewModel
import com.winyc.elo.backend.viewModel.CategoriaUi
import com.winyc.elo.backend.viewModel.CategoriaViewModel
import com.winyc.elo.backend.viewModel.Localizacao
import com.winyc.elo.backend.viewModel.UsuarioViewModel
import com.winyc.elo.telas.componentes.AvatarPerfil
import com.winyc.elo.ui.theme.EloTheme


typealias AbrirPerfil = (profissionalId: Long, nome: String, servicoId: Long?) -> Unit

private data class Categoria(val id: Long, val nome: String, val icone: ImageVector)

private data class Area(
    val id: Long,
    val nome: String,
    val icone: ImageVector,
    val servicos: List<String>
)

private data class CategoriaSelecionada(val id: Long, val titulo: String, val texto: String?)

private enum class AvaliacaoMinima(val rotulo: String, val minimo: Double) {
    Todos("Todos", 0.0),
    Quatro("4+", 4.0),
    QuatroMeio("4.5+", 4.5),
    QuatroOito("4.8+", 4.8),
}

private val VerdeDestaque = Color(0xFF12A15A)

private val ICONES: Map<String, ImageVector> = mapOf(
    "Bolt" to Icons.Outlined.Bolt,
    "CleaningServices" to Icons.Outlined.CleaningServices,
    "Plumbing" to Icons.Outlined.Plumbing,
    "Grass" to Icons.Outlined.Grass,
    "FormatPaint" to Icons.Outlined.FormatPaint,
    "Straighten" to Icons.Outlined.Straighten,
    "Foundation" to Icons.Outlined.Foundation,
    "Carpenter" to Icons.Outlined.Carpenter,
    "Handyman" to Icons.Outlined.Handyman,
    "Construction" to Icons.Outlined.Construction,
    "Window" to Icons.Outlined.Window,
    "Architecture" to Icons.Outlined.Architecture,
    "GridOn" to Icons.Outlined.GridOn,
    "VpnKey" to Icons.Outlined.VpnKey,
    "Build" to Icons.Outlined.Build,
    "TireRepair" to Icons.Outlined.TireRepair,
    "Computer" to Icons.Outlined.Computer,
    "AcUnit" to Icons.Outlined.AcUnit,
    "HomeRepairService" to Icons.Outlined.HomeRepairService,
    "Videocam" to Icons.Outlined.Videocam,
    "Router" to Icons.Outlined.Router,
    "SolarPower" to Icons.Outlined.SolarPower,
    "PestControl" to Icons.Outlined.PestControl,
    "Pool" to Icons.Outlined.Pool,
    "Elderly" to Icons.Outlined.Elderly,
    "ChildCare" to Icons.Outlined.ChildCare,
    "Restaurant" to Icons.Outlined.Restaurant,
    "Cake" to Icons.Outlined.Cake,
    "PhotoCamera" to Icons.Outlined.PhotoCamera,
    "DesignServices" to Icons.Outlined.DesignServices,
    "Checkroom" to Icons.Outlined.Checkroom,
    "ContentCut" to Icons.Outlined.ContentCut,
    "Face" to Icons.Outlined.Face,
    "Spa" to Icons.Outlined.Spa,
    "FitnessCenter" to Icons.Outlined.FitnessCenter,
    "School" to Icons.Outlined.School,
    "DirectionsCar" to Icons.Outlined.DirectionsCar,
    "DeliveryDining" to Icons.Outlined.DeliveryDining,
    "LocalShipping" to Icons.Outlined.LocalShipping,
)

fun obterIcone(nomeIcone: String?): ImageVector {
    return ICONES[nomeIcone] ?: Icons.Outlined.Work
}

/* ============================ Tela ============================ */

/** Home do cliente: busca de serviços, categorias e recomendações. */
@Composable
fun InicioScreen(
    onAbrirPerfil: AbrirPerfil = { _, _, _ -> },
    onIrParaEnderecos: () -> Unit = {},
    perfil: PerfilSessao?,
    usuarioVm: UsuarioViewModel,
    modifier: Modifier = Modifier,
    vm: CategoriaViewModel = viewModel(),
    buscaVm: BuscaViewModel = viewModel(),
) {
    var categoriaAberta by rememberSaveable(stateSaver = CategoriaSelecionadaSaver) {
        mutableStateOf<CategoriaSelecionada?>(null)
    }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val usuarioEstado by usuarioVm.estado.collectAsStateWithLifecycle()
    val homeEstado by buscaVm.home.collectAsStateWithLifecycle()

    LaunchedEffect(perfil?.id) { if (perfil != null) usuarioVm.carregar() }

    val principal = usuarioEstado.principal
    val localizacao = if (perfil != null) {
        Localizacao(principal?.latitude, principal?.longitude)
    } else {
        Localizacao.NENHUMA
    }
    LaunchedEffect(localizacao) { buscaVm.carregarHome(localizacao) }

    BackHandler(enabled = categoriaAberta != null) { categoriaAberta = null }

    if (categoriaAberta == null) {
        HomeConteudo(
            categorias = estado.categorias,
            carregandoCategorias = estado.carregando,
            erroCategorias = estado.erro,
            onTentarNovamente = vm::carregar,
            home = homeEstado,
            onRecarregarHome = buscaVm::recarregarHome,
            onAbrirCategoria = { selecao ->
                buscaVm.abrirCategoria(selecao.id.takeIf { it > 0 }, selecao.titulo, selecao.texto)
                categoriaAberta = selecao
            },
            onAbrirPerfil = onAbrirPerfil,
            modifier = modifier,
            perfil = perfil,
            enderecoPrincipal = principal,
            onIrParaEnderecos = onIrParaEnderecos,
        )
    } else {
        CategoriaScreen(
            buscaVm = buscaVm,
            onVoltar = { categoriaAberta = null },
            onAbrirPerfil = onAbrirPerfil,
            modifier = modifier,
        )
    }
}

private val CategoriaSelecionadaSaver =
    androidx.compose.runtime.saveable.listSaver<CategoriaSelecionada?, Any?>(
        save = { it?.let { s -> listOf(s.id, s.titulo, s.texto) } ?: emptyList() },
        restore = {
            if (it.isEmpty()) null
            else CategoriaSelecionada(it[0] as Long, it[1] as String, it[2] as String?)
        },
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeConteudo(
    categorias: List<CategoriaUi>,
    carregandoCategorias: Boolean,
    erroCategorias: String?,
    onTentarNovamente: () -> Unit,
    home: com.winyc.elo.backend.viewModel.BuscaHomeUi,
    onRecarregarHome: () -> Unit,
    onAbrirCategoria: (CategoriaSelecionada) -> Unit,
    perfil: PerfilSessao?,
    enderecoPrincipal: EnderecoRS?,
    onIrParaEnderecos: () -> Unit,
    onAbrirPerfil: AbrirPerfil,
    modifier: Modifier = Modifier,
) {
    val tiles = remember(categorias) {
        categorias.map { Categoria(it.idGeral, it.nomeGeral, obterIcone(it.descricaoIcon)) }
    }
    val areas = remember(categorias) {
        categorias.map { Area(it.idGeral, it.nomeGeral, obterIcone(it.descricaoIcon), it.servicos) }
    }

    PullToRefreshBox(
        isRefreshing = home.atualizando,
        onRefresh = onRecarregarHome,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
        item { HeaderBusca(perfil = perfil) }

        // Só faz sentido mostrar/gerenciar endereço para quem está logado.
        if (perfil != null) {
            item {
                SecaoEnderecoPrincipal(
                    endereco = enderecoPrincipal,
                    onIrParaEnderecos = onIrParaEnderecos,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        item {
            SecaoCategorias(
                categorias = if (tiles.isEmpty()) tiles else tiles.subList(0, minOf(8, tiles.size)),
                carregando = carregandoCategorias,
                erro = erroCategorias,
                onTentarNovamente = onTentarNovamente,
                onAbrirCategoria = { onAbrirCategoria(CategoriaSelecionada(it.id, it.nome, null)) },
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        item {
            SecaoCarrossel(
                icone = Icons.Outlined.AutoAwesome,
                titulo = "Recomendados para você",
                subtitulo = "Profissionais top avaliados perto de você",
                profissionais = home.recomendados,
                carregando = home.carregando,
                erro = home.erro,
                onTentarNovamente = onRecarregarHome,
                emAlta = false,
                onAbrirPerfil = onAbrirPerfil,
            )
        }

        item {
            SecaoCarrossel(
                icone = Icons.Outlined.LocalFireDepartment,
                titulo = "Em alta na sua região",
                subtitulo = "Populares entre clientes próximos de você",
                profissionais = home.emAlta,
                carregando = home.carregando,
                erro = null, // o erro já é sinalizado no carrossel acima
                onTentarNovamente = onRecarregarHome,
                emAlta = true,
                onAbrirPerfil = onAbrirPerfil,
            )
        }

        if (areas.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    SecaoHeader(
                        icone = Icons.Outlined.GridView,
                        titulo = "Serviços por área",
                        subtitulo = "Explore profissionais por especialidade",
                    )
                }
            }

            items(areas) { area ->
                AreaAccordion(
                    area = area,
                    onAbrirServico = { servico ->
                        onAbrirCategoria(CategoriaSelecionada(area.id, servico, servico))
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
        }
    }
}

@Composable
private fun HeaderBusca(perfil: PerfilSessao?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Olá, " + if (perfil != null && perfil.nome.isNotBlank()) perfil.nome else "Cliente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                )
                Text(
                    text = "Que serviço você precisa?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            AvatarPerfil(
                nome = if (perfil != null && perfil.nome.isNotBlank()) perfil.nome else "Cliente",
                fotoUrl = if (perfil != null && perfil.urlPerfil.isNotBlank()) perfil.urlPerfil else null,
                tamanho = 44.dp,
                fonte = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(VerdeDestaque),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Shield,
                    "Conta verificada",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Search,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Buscar serviços ou profissionais…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SecaoEnderecoPrincipal(
    endereco: EnderecoRS?,
    onIrParaEnderecos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onIrParaEnderecos),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            if (endereco != null) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_endereco_principal),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = endereco.nmApelido?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.home_endereco_titulo),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    endereco.linhaEndereco().takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_endereco_vazio_titulo),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.home_endereco_vazio_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                stringResource(R.string.home_endereco_gerenciar),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SecaoCategorias(
    categorias: List<Categoria>,
    carregando: Boolean,
    erro: String?,
    onTentarNovamente: () -> Unit,
    onAbrirCategoria: (Categoria) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SecaoHeader(
            icone = Icons.Outlined.Explore,
            titulo = "Serviços mais procurados",
            subtitulo = "As categorias mais buscadas pela comunidade",
        )
        when {
            categorias.isEmpty() && carregando -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }

            categorias.isEmpty() && erro != null -> {
                BlocoErro(mensagem = erro, onTentarNovamente = onTentarNovamente)
            }

            else -> {
                categorias.chunked(4).forEach { linha ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        linha.forEach { categoria ->
                            CategoriaTile(
                                categoria = categoria,
                                onClick = { onAbrirCategoria(categoria) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // completa a última linha para os tiles não esticarem
                        repeat(4 - linha.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriaTile(
    categoria: Categoria,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    categoria.icone,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = categoria.nome,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SecaoHeader(icone: ImageVector, titulo: String, subtitulo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icone,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecaoCarrossel(
    icone: ImageVector,
    titulo: String,
    subtitulo: String,
    profissionais: List<ProfissionalBuscaRS>,
    carregando: Boolean,
    erro: String?,
    onTentarNovamente: () -> Unit,
    emAlta: Boolean,
    onAbrirPerfil: AbrirPerfil,
) {
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        SecaoHeader(icone = icone, titulo = titulo, subtitulo = subtitulo)
        Spacer(Modifier.height(12.dp))
        when {
            profissionais.isEmpty() && carregando -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(modifier = Modifier.size(28.dp)) }
            }

            profissionais.isEmpty() && erro != null -> {
                BlocoErro(mensagem = erro, onTentarNovamente = onTentarNovamente)
            }

            profissionais.isEmpty() -> {
                Text(
                    text = "Nenhum profissional por aqui ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                ) {
                    items(profissionais, key = { it.profissionalId }) { pro ->
                        CardProfissionalHome(
                            pro = pro,
                            emAlta = emAlta,
                            onClick = {
                                onAbrirPerfil(
                                    pro.profissionalId,
                                    pro.nome,
                                    pro.servicos.firstOrNull()?.servicoId
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardProfissionalHome(pro: ProfissionalBuscaRS, emAlta: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(210.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        FotoPro(
            fotoUrl = pro.fotoPerfil,
            badgeInicio = pro.servicos.firstOrNull()?.categoriaGeral,
            emAlta = emAlta
        )
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NomeVerificado(pro.nome)
            LinhaAvaliacao(pro.avaliacao, pro.quantidadeAvaliacoes)
            pro.precoInicial?.let {
                Text(
                    text = stringResource(R.string.a_partir_de, formatarPreco(it)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (emAlta) {
                pro.distanciaKm?.let { LinhaDistancia(it) }
            } else if (pro.servicosConcluidos > 0) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${pro.servicosConcluidos} concluídos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun FotoPro(fotoUrl: String?, badgeInicio: String?, emAlta: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (fotoUrl.isNullOrBlank()) {
            Icon(
                Icons.Outlined.Image,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.size(36.dp),
            )
        } else {
            AsyncImage(
                model = fotoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        badgeInicio?.takeIf { it.isNotBlank() }?.let {
            Pill(
                texto = it,
                fundo = MaterialTheme.colorScheme.surface,
                corTexto = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
            )
        }
        if (emAlta) {
            Pill(
                texto = "Em alta",
                fundo = MaterialTheme.colorScheme.primary,
                corTexto = MaterialTheme.colorScheme.onPrimary,
                icone = Icons.Outlined.LocalFireDepartment,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
            )
        }
    }
}

@Composable
private fun Pill(
    texto: String,
    fundo: Color,
    corTexto: Color,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icone != null) {
            Icon(icone, null, tint = corTexto, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelSmall, color = corTexto, maxLines = 1)
    }
}

@Composable
private fun NomeVerificado(nome: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            nome,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.Filled.Verified,
            "Verificado",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
        )
    }
}

/** Estrelas + nº de avaliações; para quem ainda não tem nota, mostra "Novo". */
@Composable
private fun LinhaAvaliacao(avaliacao: Double?, numAvaliacoes: Int) {
    if (avaliacao == null || numAvaliacoes == 0) {
        Text(
            text = "Novo na plataforma",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star,
            null,
            tint = EloTheme.colors.avaliacao,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = formatarAvaliacao(avaliacao),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "($numAvaliacoes)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LinhaDistancia(km: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Outlined.LocationOn,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            formatarKm(km),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Bloco de erro reaproveitado com ação de "Tentar novamente". */
@Composable
private fun BlocoErro(mensagem: String, onTentarNovamente: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Tentar novamente",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AreaAccordion(
    area: Area,
    onAbrirServico: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandido by rememberSaveable(area.nome) { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        area.icone,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = area.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = area.servicos.size.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (expandido) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(visible = expandido) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    area.servicos.forEach { servico ->
                        Text(
                            text = servico,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onAbrirServico(servico) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

/* ---------------------------- Categoria (resultados) ---------------------------- */

@Composable
private fun CategoriaScreen(
    buscaVm: BuscaViewModel,
    onVoltar: () -> Unit,
    onAbrirPerfil: AbrirPerfil,
    modifier: Modifier = Modifier
) {
    val estado by buscaVm.categoria.collectAsStateWithLifecycle()
    var filtrosAbertos by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CabecalhoCategoria(
            titulo = estado.titulo,
            filtrosAbertos = filtrosAbertos,
            onVoltar = onVoltar,
            onToggleFiltros = { filtrosAbertos = !filtrosAbertos },
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (filtrosAbertos) {
                item {
                    PainelFiltros(
                        ordenacao = estado.ordenacao,
                        avaliacaoMinima = avaliacaoMinimaDe(estado.avaliacaoMinima),
                        onOrdenacao = buscaVm::mudarOrdenacao,
                        onAvaliacaoMinima = { buscaVm.mudarAvaliacaoMinima(it.minimo) },
                    )
                }
            }

            item {
                SecaoHeader(
                    icone = Icons.Outlined.AutoAwesome,
                    titulo = "Profissionais",
                    subtitulo = "Selecionados por proximidade, popularidade e avaliação",
                )
            }

            when {
                estado.profissionais.isEmpty() && estado.carregando -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator() }
                    }
                }

                estado.profissionais.isEmpty() && estado.erro != null -> {
                    item {
                        BlocoErro(
                            mensagem = estado.erro!!,
                            onTentarNovamente = buscaVm::tentarNovamenteCategoria
                        )
                    }
                }

                estado.profissionais.isEmpty() -> {
                    item {
                        Text(
                            text = "Nenhum profissional atende a esses filtros.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    }
                }

                else -> {
                    item {
                        Text(
                            text = "${estado.total} profissional(is) encontrado(s)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    items(estado.profissionais, key = { it.profissionalId }) { pro ->
                        CardProfissionalBusca(
                            pro = pro,
                            onClick = {
                                onAbrirPerfil(
                                    pro.profissionalId,
                                    pro.nome,
                                    pro.servicoDeInteresse(estado.categoriaId),
                                )
                            },
                        )
                    }
                    if (estado.temMais) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (estado.carregandoMais) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                } else {
                                    Text(
                                        text = "Carregar mais",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable(onClick = buscaVm::carregarMais)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 20.dp, vertical = 10.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CabecalhoCategoria(
    titulo: String,
    filtrosAbertos: Boolean,
    onVoltar: () -> Unit,
    onToggleFiltros: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Voltar",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onVoltar)
                .padding(6.dp),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Search,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (filtrosAbertos) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onToggleFiltros),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Tune,
                "Filtros",
                tint = if (filtrosAbertos) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PainelFiltros(
    ordenacao: OrdenacaoBusca,
    avaliacaoMinima: AvaliacaoMinima,
    onOrdenacao: (OrdenacaoBusca) -> Unit,
    onAvaliacaoMinima: (AvaliacaoMinima) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Ordenar por",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OrdenacaoBusca.entries) { op ->
                    ChipFiltro(
                        rotulo = op.rotulo,
                        selecionado = op == ordenacao,
                        onClick = { onOrdenacao(op) })
                }
            }
            Text(
                "Avaliação mínima",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AvaliacaoMinima.entries) { op ->
                    ChipFiltro(
                        rotulo = op.rotulo,
                        selecionado = op == avaliacaoMinima,
                        onClick = { onAvaliacaoMinima(op) })
                }
            }
        }
    }
}

@Composable
private fun ChipFiltro(rotulo: String, selecionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selecionado,
        onClick = onClick,
        label = {
            Text(
                rotulo,
                fontWeight = if (selecionado) FontWeight.Medium else FontWeight.Normal
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

/** Card de profissional na lista de resultados de uma categoria. */
@Composable
private fun CardProfissionalBusca(pro: ProfissionalBuscaRS, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FotoQuadrada(pro.fotoPerfil)
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                NomeVerificado(pro.nome)
                LinhaAvaliacao(pro.avaliacao, pro.quantidadeAvaliacoes)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    pro.precoInicial?.let {
                        Text(
                            text = stringResource(R.string.a_partir_de, formatarPreco(it)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    pro.distanciaKm?.let { LinhaDistancia(it) }
                }
                if (pro.servicos.isNotEmpty()) {
                    Text(
                        text = "${pro.servicos.size} serviço(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FotoQuadrada(fotoUrl: String?) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (fotoUrl.isNullOrBlank()) {
            Icon(
                Icons.Outlined.Image,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.size(28.dp),
            )
        } else {
            AsyncImage(
                model = fotoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/* ---------------------------- Formatação ---------------------------- */

private fun avaliacaoMinimaDe(valor: Double): AvaliacaoMinima =
    AvaliacaoMinima.entries.lastOrNull { valor >= it.minimo } ?: AvaliacaoMinima.Todos

private fun formatarKm(km: Double): String {
    val texto = if (km % 1.0 == 0.0) km.toInt().toString() else "%.1f".format(km)
    return "${texto.replace('.', ',')} km"
}

private fun formatarPreco(preco: Double): String =
    if (preco % 1.0 == 0.0) preco.toInt().toString() else "%.2f".format(preco).replace('.', ',')

private fun formatarAvaliacao(avaliacao: Double): String =
    if (avaliacao % 1.0 == 0.0) avaliacao.toInt().toString() else "%.1f".format(avaliacao)
        .replace('.', ',')
