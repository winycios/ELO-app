package com.winyc.elo.telas.cliente

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Foundation
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Plumbing
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winyc.elo.R
import com.winyc.elo.ui.theme.EloTheme


/** Categoria destacada em "Serviços mais procurados". */
private data class Categoria(val nome: String, val icone: ImageVector)

/** Profissional exibido nos carrosséis da home. */
private data class ProHome(
    val nome: String,
    val categoria: String,
    val avaliacao: Double,
    val numAvaliacoes: Int,
    val preco: String,
    val distancia: String,
    val requisitado: Boolean = false,
)

/** Grupo de especialidades em "Serviços por área". */
private data class Area(val nome: String, val icone: ImageVector, val servicos: List<String>)

/** Profissional na lista "Outros profissionais" da tela de categoria (filtrável). */
private data class ProFiltravel(
    val nome: String,
    val avaliacao: Double,
    val numAvaliacoes: Int,
    val precoMin: Int,
    val distanciaKm: Double,
    val servicos: Int,
)

/** Selo de um profissional recomendado (não afetado pelos filtros). */
private enum class Destaque(val rotulo: String, val subtitulo: String, val icone: ImageVector) {
    MelhorEscolha("Melhor escolha", "Alta reputação e perto de você", Icons.Filled.AutoAwesome),
    PertoPopular("Perto e popular", "Bem avaliado na sua região", Icons.Filled.Place),
    TalentoRegiao(
        "Talento da região",
        "Novo na plataforma e mora pertinho",
        Icons.Filled.WorkspacePremium
    ),
}

private data class ProDestacado(
    val nome: String,
    val avaliacao: Double,
    val numAvaliacoes: Int,
    val distanciaKm: Double,
    val precoMin: Int,
    val destaque: Destaque,
)

private enum class Ordenacao(val rotulo: String) {
    Recomendados("Recomendados"),
    Avaliacao("Avaliação"),
    Distancia("Distância"),
    Preco("Preço"),
}

private enum class AvaliacaoMinima(val rotulo: String, val minimo: Double) {
    Todos("Todos", 0.0),
    Quatro("4+", 4.0),
    QuatroMeio("4.5+", 4.5),
    QuatroOito("4.8+", 4.8),
}

private val VerdeDestaque = Color(0xFF12A15A)
private val RoxoDestaque = Color(0xFF8B5CF6)

private val CATEGORIAS = listOf(
    Categoria("Eletricista", Icons.Outlined.Bolt),
    Categoria("Diarista", Icons.Outlined.CleaningServices),
    Categoria("Encanador", Icons.Outlined.Plumbing),
    Categoria("Pintor", Icons.Outlined.Handyman),
    Categoria("Jardineiro", Icons.Outlined.Grass),
    Categoria("Montador", Icons.Outlined.Handyman),
    Categoria("Pedreiro", Icons.Outlined.Foundation),
    Categoria("Marceneiro", Icons.Outlined.Straighten),
)

private val RECOMENDADOS_HOME = listOf(
    ProHome("Carlos", "Eletricista", 4.9, 247, "150", "2,3 km", requisitado = true),
    ProHome("Roberto", "Encanador", 4.8, 183, "200", "3,1 km"),
    ProHome("Ana", "Diarista", 4.9, 321, "180", "1,8 km", requisitado = true),
)

private val EM_ALTA = listOf(
    ProHome("Sérgio", "Pintor", 4.7, 121, "160", "3,8 km"),
    ProHome("Patrícia", "Diarista", 4.7, 198, "170", "2,5 km"),
    ProHome("Rafael", "Eletricista", 4.7, 142, "140", "3,5 km"),
)

private val AREAS = listOf(
    Area(
        "Limpeza", Icons.Outlined.CleaningServices, listOf(
            "Diarista",
            "Limpeza pós-obra",
            "Limpeza de estofados",
            "Limpeza de piscina",
            "Dedetização"
        )
    ),
    Area(
        "Reformas", Icons.Outlined.Handyman, listOf(
            "Pedreiro", "Pintor", "Gesseiro", "Azulejista", "Telhadista", "Reforma geral"
        )
    ),
    Area(
        "Instalações", Icons.Outlined.Bolt, listOf(
            "Eletricista", "Encanador", "Ar-condicionado", "Antena e TV", "Câmeras", "Interfone"
        )
    ),
    Area(
        "Móveis e Montagem", Icons.Outlined.Straighten, listOf(
            "Montador", "Marceneiro", "Marido de aluguel"
        )
    ),
    Area(
        "Serviços Externos", Icons.Outlined.Grass, listOf(
            "Jardineiro", "Piscineiro", "Limpeza de calhas", "Dedetização"
        )
    ),
)

private val RECOMENDADOS_CATEGORIA = listOf(
    ProDestacado("Carlos Silva", 4.9, 247, 2.3, 80, Destaque.MelhorEscolha),
    ProDestacado("Rafael Costa", 4.7, 142, 3.5, 70, Destaque.PertoPopular),
    ProDestacado("Diego Alves", 4.5, 32, 1.2, 60, Destaque.TalentoRegiao),
)

private val OUTROS_PROFISSIONAIS = listOf(
    ProFiltravel("Fábio Nunes", 4.6, 64, 75, 5.8, 118),
    ProFiltravel("Marta Silveira", 4.8, 156, 130, 5.4, 312),
    ProFiltravel("Bruno Teixeira", 4.2, 41, 60, 1.9, 47),
    ProFiltravel("Helena Dias", 4.9, 203, 140, 6.7, 289),
    ProFiltravel("Igor Ramos", 3.9, 18, 55, 2.4, 22),
)

/* ============================ Tela ============================ */

/** Home do cliente: busca de serviços, categorias e recomendações. */
@Composable
fun InicioScreen(
    onAbrirPerfil: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var categoriaAberta by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = categoriaAberta != null) { categoriaAberta = null }

    if (categoriaAberta == null) {
        HomeConteudo(
            onAbrirCategoria = { categoriaAberta = it },
            onAbrirPerfil = onAbrirPerfil,
            modifier = modifier,
        )
    } else {
        CategoriaScreen(
            categoria = categoriaAberta!!,
            onVoltar = { categoriaAberta = null },
            onAbrirPerfil = onAbrirPerfil,
            modifier = modifier,
        )
    }
}

@Composable
private fun HomeConteudo(
    onAbrirCategoria: (String) -> Unit,
    onAbrirPerfil: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { HeaderBusca() }

        item {
            SecaoCategorias(
                onAbrirCategoria = onAbrirCategoria,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                SecaoHeader(
                    icone = Icons.Outlined.AutoAwesome,
                    titulo = "Recomendados para você",
                    subtitulo = "Profissionais top avaliados perto de você",
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                ) {
                    items(RECOMENDADOS_HOME) { pro ->
                        CardRecomendado(pro, onClick = { onAbrirPerfil(pro.nome) })
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                SecaoHeader(
                    icone = Icons.Outlined.LocalFireDepartment,
                    titulo = "Em alta na sua região",
                    subtitulo = "Populares entre clientes próximos de você",
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                ) {
                    items(EM_ALTA) { pro ->
                        CardEmAlta(pro, onClick = { onAbrirPerfil(pro.nome) })
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                SecaoHeader(
                    icone = Icons.Outlined.GridView,
                    titulo = "Serviços por área",
                    subtitulo = "Explore profissionais por especialidade",
                )
            }
        }

        items(AREAS) { area ->
            AreaAccordion(
                area = area,
                onAbrirServico = onAbrirCategoria,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun HeaderBusca() {
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
                    text = "Olá, Lucas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                )
                Text(
                    text = "Que serviço você precisa?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "LS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
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
private fun SecaoCategorias(onAbrirCategoria: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SecaoHeader(
            icone = Icons.Outlined.Explore,
            titulo = "Serviços mais procurados",
            subtitulo = "As categorias mais buscadas pela comunidade",
        )
        CATEGORIAS.chunked(4).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                linha.forEach { categoria ->
                    CategoriaTile(
                        categoria = categoria,
                        onClick = { onAbrirCategoria(categoria.nome) },
                        modifier = Modifier.weight(1f),
                    )
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
private fun CardRecomendado(pro: ProHome, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(210.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        FotoPro(badgeInicio = pro.categoria)
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NomeVerificado(pro.nome)
            LinhaAvaliacao(pro.avaliacao, pro.numAvaliacoes)
            Text(
                text = stringResource(R.string.a_partir_de, pro.preco),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (pro.requisitado) {
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
                        "Requisitado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CardEmAlta(pro: ProHome, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(210.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        FotoPro(badgeInicio = pro.categoria, emAlta = true)
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NomeVerificado(pro.nome)
            LinhaAvaliacao(pro.avaliacao, pro.numAvaliacoes)
            Text(
                text = stringResource(R.string.a_partir_de, pro.preco),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    pro.distancia,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FotoPro(badgeInicio: String, emAlta: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.Image,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(36.dp),
        )
        Pill(
            texto = badgeInicio,
            fundo = MaterialTheme.colorScheme.surface,
            corTexto = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
        )
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

@Composable
private fun LinhaAvaliacao(avaliacao: Double, numAvaliacoes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star,
            null,
            tint = EloTheme.colors.avaliacao,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = avaliacao.toString(),
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

/* ---------------------------- Categoria (filtros) ---------------------------- */
@Composable
private fun CategoriaScreen(
    categoria: String,
    onVoltar: () -> Unit,
    onAbrirPerfil: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var filtrosAbertos by rememberSaveable(categoria) { mutableStateOf(false) }
    var ordenacao by rememberSaveable(categoria) { mutableStateOf(Ordenacao.Recomendados) }
    var avaliacaoMinima by rememberSaveable(categoria) { mutableStateOf(AvaliacaoMinima.Todos) }

    val outros = remember(ordenacao, avaliacaoMinima) {
        OUTROS_PROFISSIONAIS
            .filter { it.avaliacao >= avaliacaoMinima.minimo }
            .let { lista ->
                when (ordenacao) {
                    Ordenacao.Recomendados -> lista
                    Ordenacao.Avaliacao -> lista.sortedByDescending { it.avaliacao }
                    Ordenacao.Distancia -> lista.sortedBy { it.distanciaKm }
                    Ordenacao.Preco -> lista.sortedBy { it.precoMin }
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CabecalhoCategoria(
            categoria = categoria,
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
                        ordenacao = ordenacao,
                        avaliacaoMinima = avaliacaoMinima,
                        onOrdenacao = { ordenacao = it },
                        onAvaliacaoMinima = { avaliacaoMinima = it },
                    )
                }
            }

            item {
                SecaoHeader(
                    icone = Icons.Outlined.AutoAwesome,
                    titulo = "Recomendados para você",
                    subtitulo = "Selecionados por proximidade, popularidade e oportunidade",
                )
            }

            items(RECOMENDADOS_CATEGORIA) { pro ->
                CardDestacado(pro, onClick = { onAbrirPerfil(pro.nome) })
            }

            item {
                Text(
                    text = "Outros profissionais (${outros.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (outros.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum profissional atende a esse filtro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            } else {
                items(outros) { pro ->
                    CardOutro(pro, onClick = { onAbrirPerfil(pro.nome) })
                }
            }
        }
    }
}

@Composable
private fun CabecalhoCategoria(
    categoria: String,
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
                categoria,
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
    ordenacao: Ordenacao,
    avaliacaoMinima: AvaliacaoMinima,
    onOrdenacao: (Ordenacao) -> Unit,
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
                items(Ordenacao.entries) { op ->
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

@Composable
private fun corDestaque(destaque: Destaque): Color = when (destaque) {
    Destaque.MelhorEscolha -> MaterialTheme.colorScheme.primary
    Destaque.PertoPopular -> VerdeDestaque
    Destaque.TalentoRegiao -> RoxoDestaque
}

@Composable
private fun CardDestacado(pro: ProDestacado, onClick: () -> Unit) {
    val cor = corDestaque(pro.destaque)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, cor),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FotoQuadrada()
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pro.nome,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.Verified,
                        "Verificado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Pill(
                    texto = pro.destaque.rotulo,
                    fundo = cor,
                    corTexto = Color.White,
                    icone = pro.destaque.icone,
                )
                Text(
                    pro.destaque.subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        null,
                        tint = EloTheme.colors.avaliacao,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "${pro.avaliacao} (${pro.numAvaliacoes})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        Icons.Outlined.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        formatarKm(pro.distanciaKm),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.a_partir_de, pro.precoMin),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CardOutro(pro: ProFiltravel, onClick: () -> Unit) {
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
            FotoQuadrada()
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pro.nome,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.Verified,
                        "Verificado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
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
                        pro.avaliacao.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "(${pro.numAvaliacoes} avaliações)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.a_partir_de, pro.precoMin),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        Icons.Outlined.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        formatarKm(pro.distanciaKm),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${pro.servicos} serviços",
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

@Composable
private fun FotoQuadrada() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.Image,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(28.dp),
        )
    }
}

private fun formatarKm(km: Double): String = "${km.toString().replace('.', ',')} km"