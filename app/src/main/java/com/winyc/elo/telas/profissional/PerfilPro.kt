package com.winyc.elo.telas.profissional

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.winyc.elo.R
import com.winyc.elo.backend.model.profissional.ProfissionalRS
import com.winyc.elo.backend.security.PerfilSessao
import com.winyc.elo.backend.viewModel.CategoriaViewModel
import com.winyc.elo.backend.viewModel.ProfissionalViewModel
import com.winyc.elo.telas.cliente.AjudaScreen
import com.winyc.elo.telas.componentes.AvatarPerfil
import com.winyc.elo.ui.theme.EloTheme

/* ============================ Cores de apoio dos selos ============================ */

private val Verde = Color(0xFF12A15A)
private val Azul = Color(0xFF2F6BFF)
private val Ambar = Color(0xFFDD8A15)


/** Dados públicos do profissional — o que o cliente vê e o que o Pro edita. */
internal data class PerfilPublico(
    val nome: String,
    val profissao: String,
    val fotoUrl: String,
    val bio: String,
    val area: AreaAtendimento,
    val qtdServicos: Int,
    val avaliacaoGeralPorcento: Double,
    val qtdAvalicao: Int,
    val tags: List<String>,
)

internal data class AreaAtendimento(
    val latitude: Double,
    val longitude: Double,
    val raioKm: Int,
    val cidade: String,
    val estado: String,
    val bairro: String?,
) {
    val local: String
        get() = buildString {
            if (!bairro.isNullOrBlank()) append(bairro).append(" · ")
            append(cidade)
            if (estado.isNotBlank()) append(" - ").append(estado)
        }
}

/** Formata um par de coordenadas de forma estável (ponto decimal, 5 casas). */
internal fun formatarCoord(lat: Double, lng: Double): String =
    String.format(java.util.Locale.US, "%.5f, %.5f", lat, lng)

private val AREA_PADRAO = AreaAtendimento(
    latitude = -23.52560,
    longitude = -46.69110,
    raioKm = 10,
    cidade = "São Paulo",
    estado = "SP",
    bairro = "Pompeia",
)

private val PERFIL_INICIAL = PerfilPublico(
    nome = "",
    profissao = "-",
    fotoUrl = "",
    bio = "",
    area = AREA_PADRAO,
    qtdServicos = 0,
    avaliacaoGeralPorcento = 0.0,
    qtdAvalicao = 0,
    tags = emptyList(),
)

private fun ProfissionalRS.paraPerfilPublico(
    sessao: PerfilSessao?,
    base: PerfilPublico
): PerfilPublico {
    val rs = areaAtendimentoRS
    val area = if (rs?.nrLatitude != null && rs.nrLongitude != null) {
        AreaAtendimento(
            latitude = rs.nrLatitude,
            longitude = rs.nrLongitude,
            raioKm = rs.nrRaio ?: AREA_PADRAO.raioKm,
            cidade = rs.nmCidade.orEmpty(),
            estado = rs.nmEstado.orEmpty(),
            bairro = rs.nmBairro,
        )
    } else {
        AREA_PADRAO
    }
    return base.copy(
        nome = sessao?.nome?.takeIf { it.isNotBlank() } ?: base.nome,
        fotoUrl = uriPerfil?.takeIf { it.isNotBlank() } ?: sessao?.urlPerfilPro?.takeIf { it.isNotBlank() } ?: base.fotoUrl,
        bio = apresentacao.orEmpty(),
        area = area,
        tags = dsEspecialidades?.split(';', ',')?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty(),
        qtdAvalicao = qtRespostaGeral ?: 0,
        qtdServicos = qtServicos ?: 0,
        avaliacaoGeralPorcento = 0.0

    )
}

/* ============================ Tela ============================ */

private enum class SheetPro { EditarPerfil, MeusServicos }

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PerfilProScreen(
    sessao: PerfilSessao?,
    onSair: () -> Unit = {},
    modifier: Modifier = Modifier,
    servicoVm: ProfissionalViewModel = viewModel(),
    categoriaVm: CategoriaViewModel = viewModel(),
) {
    var perfil by remember(sessao) {
        mutableStateOf(
            PERFIL_INICIAL.copy(
                nome = sessao?.nome?.takeIf { it.isNotBlank() } ?: PERFIL_INICIAL.nome,
                fotoUrl = sessao?.urlPerfilPro?.takeIf { it.isNotBlank() } ?: PERFIL_INICIAL.fotoUrl,
            ),
        )
    }
    var disponivel by rememberSaveable { mutableStateOf(true) }
    var sheet by remember { mutableStateOf<SheetPro?>(null) }
    var mostrarAjuda by rememberSaveable { mutableStateOf(false) }

    val servicoEstado by servicoVm.estado.collectAsStateWithLifecycle()
    val categoriaEstado by categoriaVm.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        servicoVm.carregar()
        servicoVm.carregarPerfil()
    }

    val perfilRs = servicoEstado.perfil
    LaunchedEffect(perfilRs, sessao) {
        if (perfilRs != null) {
            perfil = perfilRs.paraPerfilPublico(sessao, perfil)
            perfilRs.stDisponivel?.let { disponivel = it }
        }
    }

    BackHandler(enabled = mostrarAjuda) { mostrarAjuda = false }
    if (mostrarAjuda) {
        AjudaScreen(onVoltar = { mostrarAjuda = false }, modifier = modifier)
        return
    }

    val nomesGerais = remember(categoriaEstado.categoriasRaw) {
        categoriaEstado.categoriasRaw
            .flatMap { it.categoriaEspecificaList }
            .associate { it.categoriaGeral.id to it.categoriaGeral.nmCategoria }
    }
    val gerais = servicoEstado.servicos.mapNotNull { it.categoria?.idCategoriaGeral }.distinct()
    val categorias = gerais.size
    val areas =
        gerais.mapNotNull { nomesGerais[it] }.joinToString(", ").ifBlank { perfil.profissao }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            CabecalhoPro(
                perfil = perfil,
                areas = areas,
                onEditar = { sheet = SheetPro.EditarPerfil })
        }
        item {
            // Só pode ficar disponível (aparecer nas buscas) quem tem destino
            // (área de atendimento com coordenadas) e ao menos um serviço.
            val temArea = perfilRs?.areaAtendimentoRS
                ?.let { it.nrLatitude != null && it.nrLongitude != null } == true
            val temServico = servicoEstado.servicos.isNotEmpty()
            val carregandoRequisitos = servicoEstado.carregando || servicoEstado.carregandoPerfil
            CardDisponibilidade(
                disponivel = disponivel,
                temArea = temArea,
                temServico = temServico,
                carregando = carregandoRequisitos,
                onMudar = { novo ->
                    if (novo && !(temArea && temServico)) return@CardDisponibilidade
                    disponivel = novo
                    servicoVm.definirDisponibilidade(novo) { ok -> if (!ok) disponivel = !novo }
                },
                onResolverArea = { sheet = SheetPro.EditarPerfil },
                onResolverServico = { sheet = SheetPro.MeusServicos },
            )
        }
        item {
            CardAcao(
                icone = Icons.Outlined.WorkOutline,
                titulo = stringResource(R.string.pro_meus_servicos),
                subtitulo = stringResource(
                    R.string.pro_contagem_servicos,
                    categorias,
                    servicoEstado.servicos.size
                ),
                onClick = { sheet = SheetPro.MeusServicos },
            )
        }
        item { LinhaSelos() }
        item { CardAreaAtendimento(area = perfil.area) }
        item {
            CardAcao(
                icone = Icons.AutoMirrored.Outlined.HelpOutline,
                titulo = stringResource(R.string.pro_ajuda),
                subtitulo = stringResource(R.string.pro_ajuda_sub),
                onClick = { mostrarAjuda = true },
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onSair)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.perfil_sair),
                    style = MaterialTheme.typography.titleSmall,
                    color = EloTheme.colors.coral,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Outlined.Logout,
                    null,
                    tint = EloTheme.colors.coral,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }

    when (sheet) {
        SheetPro.EditarPerfil -> EditarPerfilPublicoSheet(
            perfil = perfil,
            vm = servicoVm,
            carregando = perfilRs == null && servicoEstado.carregandoPerfil,
            onSalvar = { perfil = it },
            onFechar = { sheet = null },
        )

        SheetPro.MeusServicos -> MeusServicosSheet(
            vm = servicoVm,
            categorias = categoriaEstado.categoriasRaw,
            onFechar = { sheet = null },
        )

        null -> Unit
    }
}

@Composable
private fun CabecalhoPro(perfil: PerfilPublico, areas: String, onEditar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPerfil(
                    nome = perfil.nome,
                    fotoUrl = perfil.fotoUrl.ifEmpty { null },
                    tamanho = 64.dp,
                    fonte = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            perfil.nome,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Verified,
                            stringResource(R.string.pro_badge_verificado),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        areas,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${perfil.avaliacaoGeralPorcento} (${perfil.qtdAvalicao} ${stringResource(R.string.avaliacoes)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = onEditar)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.pro_editar_perfil_publico),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.Edit,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun EstatItem(valor: String, rotulo: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            rotulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CardDisponibilidade(
    disponivel: Boolean,
    temArea: Boolean,
    temServico: Boolean,
    carregando: Boolean,
    onMudar: (Boolean) -> Unit,
    onResolverArea: () -> Unit,
    onResolverServico: () -> Unit,
) {
    val podeHabilitar = temArea && temServico
    // Já disponível pode sempre desligar; para ligar, exige cadastro completo.
    val switchHabilitado = !carregando && (disponivel || podeHabilitar)
    val mostrarPendencias = !carregando && !podeHabilitar && !disponivel

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.pro_disponivel_titulo),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        stringResource(
                            if (disponivel) R.string.pro_disponivel_sub_on else R.string.pro_disponivel_sub_off
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = disponivel,
                    onCheckedChange = onMudar,
                    enabled = switchHabilitado,
                )
            }

            if (mostrarPendencias) {
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.pro_disponivel_bloqueado),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                if (!temArea) {
                    PendenciaDisponibilidade(
                        icone = Icons.Outlined.LocationOn,
                        texto = stringResource(R.string.pro_disponivel_falta_area),
                        onClick = onResolverArea,
                    )
                }
                if (!temServico) {
                    if (!temArea) Spacer(Modifier.height(8.dp))
                    PendenciaDisponibilidade(
                        icone = Icons.Outlined.WorkOutline,
                        texto = stringResource(R.string.pro_disponivel_falta_servico),
                        onClick = onResolverServico,
                    )
                }
            }
        }
    }
}

/** Item de pendência clicável que leva o Pro à tela que resolve o requisito. */
@Composable
private fun PendenciaDisponibilidade(icone: ImageVector, texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EloTheme.colors.coral.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icone, null, tint = EloTheme.colors.coral, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun CardAcao(icone: ImageVector, titulo: String, subtitulo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icone,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
private fun LinhaSelos() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Selo(
            Icons.Filled.Verified,
            Verde,
            stringResource(R.string.pro_badge_verificado),
            Modifier.weight(1f)
        )
        Selo(
            Icons.Outlined.EmojiEvents,
            Ambar,
            stringResource(R.string.pro_badge_top),
            Modifier.weight(1f)
        )
        Selo(
            Icons.Outlined.Schedule,
            Azul,
            stringResource(R.string.pro_badge_pontual),
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun Selo(icone: ImageVector, cor: Color, texto: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(cor.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icone, null, tint = cor, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            color = cor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CardAreaAtendimento(area: AreaAtendimento) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.pro_area_atendimento),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(R.string.pro_area_ate_km, area.raioKm),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Text(
                area.local,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                formatarCoord(area.latitude, area.longitude),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
