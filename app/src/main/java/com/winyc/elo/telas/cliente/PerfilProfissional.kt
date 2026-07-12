package com.winyc.elo.telas.cliente

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.winyc.elo.ui.theme.EloTheme

/* ============================ Modelo (mock) ============================ */

private data class ServicoPro(
    val numero: Int,
    val nome: String,
    val precoMin: Int,
    val precoMax: Int,
    val descricao: String,
    val diferenciais: List<String>,
)

private enum class Sentimento(val rotulo: String) {
    Positivo("Positivo"),
    Neutro("Neutro"),
    Negativo("Negativo"),
}

private data class AvaliacaoPro(
    val autor: String,
    val data: String,
    val nota: Int,
    val texto: String,
    val uteis: Int,
    val sentimento: Sentimento,
)

// Cores de apoio (fora do contexto coral/teal, iguais às usadas na home).
private val Verde = Color(0xFF12A15A)
private val Azul = Color(0xFF2F6BFF)
private val Roxo = Color(0xFF8B5CF6)
private val Amarelo = Color(0xFFB98900)
private val Vermelho = Color(0xFFE05353)

private const val CATEGORIA_DEMO = "Eletricista"
private const val AVALIACAO_DEMO = 4.9
private const val NUM_AVALIACOES = 247
private const val FAIXA_PRECO = "R$ 80 - R$ 150"

// Número sentinela usado quando o cliente escolhe "Outro serviço" (descreve livremente).
private const val OUTRO_SERVICO = 0

private const val SOBRE_DEMO =
    "Eletricista profissional com mais de 15 anos de experiência. " +
    "Especializado em instalações residenciais e comerciais, " +
    "manutenção preventiva e corretiva."

private val CLIENTES_DESTACAM =
    listOf("Pontual", "Organizado", "Excelente trabalho", "Justo no preço")

private val SERVICOS = listOf(
    ServicoPro(
        1, "Instalação elétrica residencial", 150, 400,
        "Instalação completa de pontos de luz, tomadas e quadros de distribuição " +
            "para residências, seguindo as normas de segurança.",
        listOf("Material de primeira linha", "Garantia de 90 dias", "Limpeza ao final"),
    ),
    ServicoPro(
        2, "Manutenção e reparo elétrico", 100, 250,
        "Diagnóstico e correção de curtos, disjuntores que desarmam, tomadas " +
            "queimadas e demais problemas elétricos.",
        listOf("Atendimento no mesmo dia", "Diagnóstico incluso", "Orçamento sem compromisso"),
    ),
    ServicoPro(
        3, "Instalação de chuveiro elétrico", 120, 180,
        "Troca e instalação de chuveiros elétricos com dimensionamento correto " +
            "de fiação e disjuntor.",
        listOf("Fiação dimensionada", "Teste de segurança", "Garantia de 90 dias"),
    ),
)

private val AVALIACOES = listOf(
    AvaliacaoPro(
        "Maria L.", "12/04/2026", 5,
        "Carlos foi extremamente pontual e resolveu o problema elétrico da minha " +
            "casa rapidamente. Trabalho impecável!",
        24, Sentimento.Positivo,
    ),
    AvaliacaoPro(
        "João P.", "08/04/2026", 5,
        "Excelente profissional. Muito organizado, deixou tudo limpo após o " +
            "serviço. Preço justo pelo trabalho realizado.",
        18, Sentimento.Positivo,
    ),
    AvaliacaoPro(
        "Lúcia S.", "02/04/2026", 4,
        "Bom serviço no geral. Demorou um pouco mais do que o combinado, mas o " +
            "resultado ficou ótimo.",
        7, Sentimento.Neutro,
    ),
    AvaliacaoPro(
        "Rafael M.", "28/03/2026", 5,
        "Recomendo demais! Explicou tudo com calma e o preço foi honesto.",
        12, Sentimento.Positivo,
    ),
    AvaliacaoPro(
        "Beatriz R.", "20/03/2026", 5,
        "Serviço rápido e bem feito. Voltarei a chamar com certeza.",
        9, Sentimento.Positivo,
    ),
    AvaliacaoPro(
        "Igor T.", "11/03/2026", 2,
        "O serviço ficou bom, mas o profissional atrasou bastante e não avisou.",
        3, Sentimento.Negativo,
    ),
)

private val HISTOGRAMA = listOf(5 to 0.82f, 4 to 0.28f, 3 to 0.20f, 2 to 0.06f, 1 to 0.04f)

private fun primeiroNome(nome: String): String = nome.trim().split(" ").first()

/* ============================ Tela ============================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilProfissionalScreen(
    nome: String,
    onVoltar: () -> Unit,
    onIrParaInicio: () -> Unit = {},
    onVerPedidos: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var verTodasAvaliacoes by rememberSaveable(nome) { mutableStateOf(false) }
    var escolhendoServico by rememberSaveable(nome) { mutableStateOf(false) }
    var servicoDetalhe by rememberSaveable(nome) { mutableStateOf<Int?>(null) }
    var servicoInfo by rememberSaveable(nome) { mutableStateOf<Int?>(null) }
    var sucesso by rememberSaveable(nome) { mutableStateOf(false) }

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
            nome = nome,
            onInicio = onIrParaInicio,
            onPedidos = onVerPedidos,
            modifier = modifier,
        )

        servicoDetalhe != null -> DetalhesServicoScreen(
            nome = nome,
            servico = SERVICOS.firstOrNull { it.numero == servicoDetalhe },
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
            modifier = modifier,
        )

        escolhendoServico -> EscolherServicoScreen(
            nome = nome,
            onVoltar = { escolhendoServico = false },
            onAbrirInfo = { numero -> servicoInfo = numero },
            onOutroServico = { servicoDetalhe = OUTRO_SERVICO },
            modifier = modifier,
        )

        verTodasAvaliacoes -> AvaliacoesScreen(
            nome = nome,
            onVoltar = { verTodasAvaliacoes = false },
            modifier = modifier,
        )

        else -> Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 150.dp),
            ) {
                item {
                    Column {
                        HeroCarrossel(nome = nome, onVoltar = onVoltar)
                        CardEstatisticas(
                            modifier = Modifier
                                .offset(y = (-28).dp)
                                .padding(horizontal = 16.dp),
                        )
                    }
                }

                item {
                    SecaoSobre(modifier = Modifier.padding(horizontal = 16.dp))
                }

                item {
                    Spacer(Modifier.height(20.dp))
                    SecaoServicos(
                        onAbrirServico = { servicoInfo = it },
                        onVerTodos = { escolhendoServico = true },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    SecaoAvaliacoes(
                        onVerTodas = { verTodasAvaliacoes = true },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            BarraContratar(
                nome = nome,
                onContratar = { escolhendoServico = true },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    servicoInfo?.let { numero ->
        ServicoSheet(
            servico = SERVICOS.first { it.numero == numero },
            onFechar = { servicoInfo = null },
            onContratar = {
                servicoInfo = null
                servicoDetalhe = numero
            },
        )
    }
}

@Composable
private fun HeroCarrossel(nome: String, onVoltar: () -> Unit) {
    val paginas = 3
    val pagerState = rememberPagerState(pageCount = { paginas })

    Box(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(state = pagerState) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Image,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(44.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.BottomCenter)
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
                text = "${pagerState.currentPage + 1}/$paginas",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }

        // Identificação do profissional
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = iniciais(nome),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                    )
                }
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
                        Icon(
                            Icons.Filled.Star,
                            null,
                            tint = EloTheme.colors.avaliacao,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$AVALIACAO_DEMO ($NUM_AVALIACOES)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            Icons.Outlined.LocationOn,
                            null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "2,3 km",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(paginas) { indice ->
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
private fun CardEstatisticas(modifier: Modifier = Modifier) {
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
            Estatistica(Icons.Outlined.Shield, Verde, "97%", "Reputação")
            Estatistica(Icons.Outlined.ThumbUp, Azul, "512", "Serviços")
            Estatistica(Icons.Outlined.Schedule, Roxo, "15 anos", "Experiência")
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
        Text(valor, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(rotulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* ---------------------------- Sobre ---------------------------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecaoSobre(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Sobre", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(
            SOBRE_DEMO,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Clientes destacam:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CLIENTES_DESTACAM.forEach { destaque ->
                Text(
                    text = destaque,
                    style = MaterialTheme.typography.labelLarge,
                    color = Verde,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Verde.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
        BannerRequisitado()
    }
}

@Composable
private fun BannerRequisitado() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.TrendingUp,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Muito requisitado — agende logo!",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/* ---------------------------- Serviços ---------------------------- */

@Composable
private fun SecaoServicos(
    onAbrirServico: (Int) -> Unit,
    onVerTodos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Serviços oferecidos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = CATEGORIA_DEMO,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        Text(
            "Toque em um serviço para ver descrição, valor e diferenciais.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SERVICOS.take(3).forEach { servico ->
            CardServico(servico = servico, onClick = { onAbrirServico(servico.numero) })
        }
        OutlinedButton(
            onClick = onVerTodos,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Outlined.Layers, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ver todos os serviços")
            Spacer(Modifier.width(6.dp))
            Text(
                "(${SERVICOS.size})",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Card compacto do perfil (img_1): número, nome, faixa de preço e seta. */
@Composable
private fun CardServico(servico: ServicoPro, onClick: () -> Unit) {
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
                    servico.numero.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    servico.nome,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "R$ ${servico.precoMin} - R$ ${servico.precoMax}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
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

/** Card rico de serviço (ícone, descrição, faixa de preço e diferenciais). */
@Composable
private fun CardServicoDisponivel(servico: ServicoPro, onClick: () -> Unit) {
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
                    servico.nome,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    servico.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "R$ ${servico.precoMin} - R$ ${servico.precoMax}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                DiferenciaisResumo(servico.diferenciais)
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

/** Mostra os 2 primeiros diferenciais como chips e resume o resto em "+N". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiferenciaisResumo(diferenciais: List<String>) {
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

/* ---------------------------- Avaliações (prévia) ---------------------------- */

@Composable
private fun SecaoAvaliacoes(onVerTodas: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Avaliações",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                AVALIACAO_DEMO.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Estrelas(5)
                Text(
                    "$NUM_AVALIACOES avaliações · 67% positivas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AVALIACOES.take(2).forEach { avaliacao ->
            CardAvaliacaoCompacto(avaliacao)
        }
    }
}

@Composable
private fun CardAvaliacaoCompacto(avaliacao: AvaliacaoPro) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    avaliacao.autor,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    avaliacao.data,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Estrelas(avaliacao.nota)
            Text(
                avaliacao.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* ---------------------------- Barra fixa: Contratar ---------------------------- */

@Composable
private fun BarraContratar(nome: String, onContratar: () -> Unit, modifier: Modifier = Modifier) {
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
                        "Faixa de preço",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        FAIXA_PRECO,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    Icons.Filled.Star,
                    null,
                    tint = EloTheme.colors.avaliacao,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "$AVALIACAO_DEMO ($NUM_AVALIACOES)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
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
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

/* ---------------------------- Tela: todas as avaliações ---------------------------- */

@Composable
private fun AvaliacoesScreen(nome: String, onVoltar: () -> Unit, modifier: Modifier = Modifier) {
    var filtro by rememberSaveable(nome) { mutableStateOf<Sentimento?>(null) }

    val lista = remember(filtro) {
        if (filtro == null) AVALIACOES else AVALIACOES.filter { it.sentimento == filtro }
    }

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

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { CardResumoAvaliacoes() }
            item {
                FiltrosSentimento(selecionado = filtro, onSelecionar = { filtro = it })
            }
            items(lista) { avaliacao ->
                CardAvaliacaoCompleto(avaliacao)
            }
        }
    }
}

@Composable
private fun CardResumoAvaliacoes() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    AVALIACAO_DEMO.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Estrelas(5)
                Text(
                    "$NUM_AVALIACOES avaliações",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(20.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HISTOGRAMA.forEach { (estrelas, fracao) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            estrelas.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(12.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fracao)
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
}

@Composable
private fun FiltrosSentimento(selecionado: Sentimento?, onSelecionar: (Sentimento?) -> Unit) {
    val opcoes: List<Pair<String, Sentimento?>> = listOf(
        "Todas (${AVALIACOES.size})" to null,
        "Positivo (${AVALIACOES.count { it.sentimento == Sentimento.Positivo }})" to Sentimento.Positivo,
        "Neutro (${AVALIACOES.count { it.sentimento == Sentimento.Neutro }})" to Sentimento.Neutro,
        "Negativo (${AVALIACOES.count { it.sentimento == Sentimento.Negativo }})" to Sentimento.Negativo,
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(opcoes) { (rotulo, sentimento) ->
            val ativo = selecionado == sentimento
            FilterChip(
                selected = ativo,
                onClick = { onSelecionar(sentimento) },
                label = { Text(rotulo, fontWeight = if (ativo) FontWeight.Medium else FontWeight.Normal) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = ativo,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
private fun CardAvaliacaoCompleto(avaliacao: AvaliacaoPro) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        avaliacao.autor.first().uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        avaliacao.autor,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        avaliacao.data,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SeloSentimento(avaliacao.sentimento)
            }
            Estrelas(avaliacao.nota)
            Text(
                avaliacao.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.ThumbUp,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "${avaliacao.uteis} acharam útil",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SeloSentimento(sentimento: Sentimento) {
    val cor = when (sentimento) {
        Sentimento.Positivo -> Verde
        Sentimento.Neutro -> Amarelo
        Sentimento.Negativo -> Vermelho
    }
    Text(
        text = sentimento.rotulo,
        style = MaterialTheme.typography.labelSmall,
        color = cor,
        modifier = Modifier
            .clip(CircleShape)
            .background(cor.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/* ---------------------------- Fluxo: escolher serviço (img_3) ---------------------------- */

@Composable
private fun EscolherServicoScreen(
    nome: String,
    onVoltar: () -> Unit,
    onAbrirInfo: (Int) -> Unit,
    onOutroServico: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CabecalhoFluxo(
            titulo = "Qual serviço você precisa?",
            subtitulo = "Selecione ou descreva livremente",
            onVoltar = onVoltar,
        )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ProHeaderCard(nome) }
            item { RotuloSecao("SERVIÇOS DISPONÍVEIS") }
            items(SERVICOS) { servico ->
                CardServicoDisponivel(servico = servico, onClick = { onAbrirInfo(servico.numero) })
            }
            item { CardOutroServico(onClick = onOutroServico) }
        }
    }
}

@Composable
private fun CardOutroServico(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .bordaTracejada(MaterialTheme.colorScheme.outline, 14.dp)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Description,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Outro serviço",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Não encontrou o que precisa? Descreva livremente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/* ---------------------------- Fluxo: detalhes do serviço (img_4) ---------------------------- */

@Composable
private fun DetalhesServicoScreen(
    nome: String,
    servico: ServicoPro?,
    onVoltar: () -> Unit,
    onTrocar: () -> Unit,
    onConfirmar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var descricao by rememberSaveable(servico?.numero) { mutableStateOf("") }
    val podeConfirmar = servico != null || descricao.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CabecalhoFluxo(
            titulo = "Detalhes do serviço",
            subtitulo = "Informe data, horário e descrição",
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
                        onValueChange = { descricao = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Descreva livremente o que você precisa…") },
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
            item { SecaoImagens() }
            item { SecaoDataHorario() }
            item { BannerContratacaoSegura() }
        }
        BarraConfirmar(habilitado = podeConfirmar, onConfirmar = onConfirmar)
    }
}

@Composable
private fun ServicoSelecionadoBanner(servico: ServicoPro?, onTrocar: () -> Unit) {
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
                servico?.nome ?: "Outro serviço",
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
private fun SecaoImagens() {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CampoRotulo(Icons.Outlined.Image, "Adicionar imagens")
            Spacer(Modifier.weight(1f))
            Text(
                "0/3",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .bordaTracejada(MaterialTheme.colorScheme.primary, 12.dp)
                .clickable { Toast.makeText(context, "Em breve", Toast.LENGTH_SHORT).show() }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.AddPhotoAlternate,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Adicionar até 3 fotos",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            "Envie fotos do local ou do problema para ajudar o profissional a entender melhor o serviço.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SecaoDataHorario() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoRotulo(Icons.Outlined.CalendarToday, "Data preferida")
            CampoSelecao(texto = "dd/mm/aaaa", comSeta = false, modifier = Modifier.fillMaxWidth())
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoRotulo(Icons.Outlined.Schedule, "Horário")
            CampoSelecao(texto = "Selecione", comSeta = true, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun CampoSelecao(texto: String, comSeta: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
            .clickable { Toast.makeText(context, "Em breve", Toast.LENGTH_SHORT).show() }
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (comSeta) {
            Icon(
                Icons.Filled.KeyboardArrowDown,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
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
private fun BarraConfirmar(habilitado: Boolean, onConfirmar: () -> Unit) {
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
                enabled = habilitado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Confirmar solicitação", style = MaterialTheme.typography.titleMedium)
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
            Text(titulo, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    iniciais(nome),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        null,
                        tint = EloTheme.colors.avaliacao,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$AVALIACAO_DEMO ($NUM_AVALIACOES)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = CATEGORIA_DEMO,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun CampoRotulo(icone: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
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

private fun Modifier.bordaTracejada(cor: Color, raio: Dp): Modifier = drawBehind {
    drawRoundRect(
        color = cor,
        cornerRadius = CornerRadius(raio.toPx()),
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
        ),
    )
}

/* ---------------------------- Detalhes rápidos do serviço (bottom sheet) ---------------------------- */

/**
 * Abre ao tocar num serviço do perfil e mostra as principais informações
 * (descrição, faixa de preço e diferenciais), com atalho para solicitar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ServicoSheet(
    servico: ServicoPro,
    onFechar: () -> Unit,
    onContratar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val faixa = "R$ ${servico.precoMin} - R$ ${servico.precoMax}"

    ModalBottomSheet(onDismissRequest = onFechar, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Cabeçalho: número + nome + faixa de preço + recolher.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        servico.numero.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        servico.nome,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        faixa,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
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

            Text(
                servico.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Faixa de valor em destaque.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Faixa de valor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    faixa,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }

            RotuloSecao("PONTOS PRINCIPAIS")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                servico.diferenciais.forEach { ChipDiferencial(it) }
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

/* ---------------------------- Solicitação enviada (sucesso) ---------------------------- */

@Composable
private fun SolicitacaoEnviadaScreen(
    nome: String,
    onInicio: () -> Unit,
    onPedidos: () -> Unit,
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
            onClick = onPedidos,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.AutoMirrored.Outlined.ReceiptLong, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ver meus pedidos", style = MaterialTheme.typography.titleMedium)
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

private fun iniciais(nome: String): String =
    nome.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")