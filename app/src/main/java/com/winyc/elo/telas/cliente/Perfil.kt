package com.winyc.elo.telas.cliente

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.winyc.elo.R

/* ============================ Cores de apoio / mock ============================ */

private val Azul = Color(0xFF2F6BFF)
private val Roxo = Color(0xFF8B5CF6)
private val Verde = Color(0xFF12A15A)
private val Amarelo = Color(0xFFDD8A15)

private const val USUARIO_NOME = "Lucas Silva"
private const val USUARIO_EMAIL = "lucas.silva@email.com"
private const val USUARIO_TELEFONE = "(11) 98765-4321"
private const val USUARIO_LOCAL = "São Paulo, SP"

private enum class TipoEndereco(val rotuloRes: Int, val icone: ImageVector, val cor: Color) {
    Casa(R.string.endereco_tipo_casa, Icons.Outlined.Home, Azul),
    Trabalho(R.string.endereco_tipo_trabalho, Icons.Outlined.Work, Roxo),
    Outro(R.string.endereco_tipo_outro, Icons.Outlined.LocationOn, Amarelo),
}

private data class EnderecoMock(
    val id: Int,
    val nome: String,
    val tipo: TipoEndereco,
    val logradouro: String,
    val bairroCidade: String,
    val cep: String,
    val principal: Boolean,
)

private val ENDERECOS_INICIAIS = listOf(
    EnderecoMock(
        1, "Minha casa", TipoEndereco.Casa,
        "Rua das Flores, 123 – Apto 42", "Jardim América, São Paulo – SP", "01401-000",
        principal = true,
    ),
    EnderecoMock(
        2, "Escritório", TipoEndereco.Trabalho,
        "Av. Paulista, 1578 – 14º andar", "Bela Vista, São Paulo – SP", "01310-200",
        principal = false,
    ),
)

private data class Faq(val categoria: String, val pergunta: String)

private const val FAQ_TODOS = "Todos"

private val FAQS = listOf(
    Faq("Contratação", "Como contratar um profissional?"),
    Faq("Contratação", "Posso cancelar um pedido?"),
    Faq("Pagamento", "Quais formas de pagamento são aceitas?"),
    Faq("Pagamento", "Quando o profissional recebe o pagamento?"),
    Faq("Segurança", "Como a plataforma garante minha segurança?"),
    Faq("Segurança", "O que é o Programa de Proteção ao Cliente?"),
    Faq("Avaliações", "Como funciona o sistema de avaliações?"),
    Faq("Conta", "Como alterar meus dados cadastrais?"),
)

private val FAQ_CATEGORIAS =
    listOf(FAQ_TODOS) + FAQS.map { it.categoria }.distinct()

/* ============================ Router (logado x deslogado) ============================ */

/** Perfil do cliente. Deslogado mostra o convite para entrar; logado abre a área de conta. */
@Composable
fun PerfilScreen(
    logado: Boolean,
    onAbrirLogin: () -> Unit,
    onSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (logado) {
        PerfilLogado(onAbrirLogin = onAbrirLogin, onSair = onSair, modifier = modifier)
    } else {
        PerfilDeslogado(onAbrirLogin = onAbrirLogin, modifier = modifier)
    }
}

@Composable
private fun PerfilDeslogado(onAbrirLogin: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = stringResource(R.string.perfil_titulo),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.perfil_titulo),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.size(8.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onAbrirLogin)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Login,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.perfil_entrar_criar_conta),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/* ============================ Logado (navegação interna) ============================ */

private enum class PerfilAba { Menu, EditarPerfil, Enderecos, Ajuda }

@Composable
private fun PerfilLogado(
    onAbrirLogin: () -> Unit,
    onSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var aba by rememberSaveable { mutableStateOf(PerfilAba.Menu) }

    BackHandler(enabled = aba != PerfilAba.Menu) { aba = PerfilAba.Menu }

    when (aba) {
        PerfilAba.Menu -> MenuPerfil(
            onEditar = { aba = PerfilAba.EditarPerfil },
            onEnderecos = { aba = PerfilAba.Enderecos },
            onAjuda = { aba = PerfilAba.Ajuda },
            onCriarConta = onAbrirLogin,
            onSair = onSair,
            modifier = modifier,
        )

        PerfilAba.EditarPerfil -> EditarPerfilScreen(
            onVoltar = { aba = PerfilAba.Menu },
            modifier = modifier,
        )

        PerfilAba.Enderecos -> EnderecosFlow(
            onVoltar = { aba = PerfilAba.Menu },
            modifier = modifier,
        )

        PerfilAba.Ajuda -> AjudaScreen(
            onVoltar = { aba = PerfilAba.Menu },
            modifier = modifier,
        )
    }
}

/* ---------------------------- Menu principal (img) ---------------------------- */

@Composable
private fun MenuPerfil(
    onEditar: () -> Unit,
    onEnderecos: () -> Unit,
    onAjuda: () -> Unit,
    onCriarConta: () -> Unit,
    onSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarIniciais(
                    USUARIO_NOME,
                    tamanho = 68.dp,
                    fonte = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            USUARIO_NOME,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Verified,
                            stringResource(R.string.perfil_verificado),
                            tint = Verde,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            USUARIO_LOCAL,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onEditar)
                            .padding(vertical = 2.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.perfil_editar),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        item { CardEstatisticasPerfil() }

        item { CardContato() }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column {
                    ItemMenu(
                        icone = Icons.Outlined.LocationOn,
                        titulo = stringResource(R.string.perfil_enderecos),
                        subtitulo = stringResource(R.string.perfil_enderecos_sub),
                        onClick = onEnderecos,
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 66.dp),
                    )
                    ItemMenu(
                        icone = Icons.AutoMirrored.Outlined.HelpOutline,
                        titulo = stringResource(R.string.perfil_ajuda),
                        subtitulo = stringResource(R.string.perfil_ajuda_sub),
                        onClick = onAjuda,
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onSair)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Logout,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.perfil_sair),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun CardEstatisticasPerfil() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 16.dp),
        ) {
            EstatItem("5", stringResource(R.string.perfil_stat_pedidos), Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline)
            EstatItem("3", stringResource(R.string.perfil_stat_concluidos), Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline)
            EstatItem("4.9", stringResource(R.string.perfil_stat_avaliacao), Modifier.weight(1f))
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
private fun CardContato() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinhaContato(Icons.Outlined.MailOutline, stringResource(R.string.perfil_email), USUARIO_EMAIL)
            LinhaContato(Icons.Outlined.Phone, stringResource(R.string.perfil_telefone), USUARIO_TELEFONE)
        }
    }
}

@Composable
private fun LinhaContato(icone: ImageVector, rotulo: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icone,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                rotulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                valor,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ItemMenu(
    icone: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
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

/* ---------------------------- Editar perfil (img_1) ---------------------------- */

@Composable
private fun EditarPerfilScreen(onVoltar: () -> Unit, modifier: Modifier = Modifier) {
    var nome by rememberSaveable { mutableStateOf(USUARIO_NOME) }
    var email by rememberSaveable { mutableStateOf(USUARIO_EMAIL) }
    var telefone by rememberSaveable { mutableStateOf(USUARIO_TELEFONE) }
    var endereco by rememberSaveable { mutableStateOf(USUARIO_LOCAL) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopBarVoltar(titulo = stringResource(R.string.perfil_editar), subtitulo = null, onVoltar = onVoltar)

        LazyColumn(
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AvatarIniciais(
                            nome,
                            tamanho = 96.dp,
                            fonte = MaterialTheme.typography.headlineMedium
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.PhotoCamera,
                                stringResource(R.string.perfil_trocar_foto),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            item {
                CampoTexto(
                    stringResource(R.string.perfil_campo_nome),
                    nome,
                    { nome = it },
                    stringResource(R.string.perfil_campo_nome_hint),
                )
            }
            item {
                CampoTexto(
                    stringResource(R.string.perfil_email),
                    email,
                    { email = it },
                    stringResource(R.string.perfil_campo_email_hint),
                    teclado = KeyboardType.Email
                )
            }
            item {
                CampoTexto(
                    stringResource(R.string.perfil_telefone),
                    telefone,
                    { telefone = it },
                    stringResource(R.string.perfil_campo_telefone_hint),
                    teclado = KeyboardType.Phone
                )
            }
            item {
                CampoTexto(
                    stringResource(R.string.perfil_campo_endereco),
                    endereco,
                    { endereco = it },
                    stringResource(R.string.perfil_campo_endereco_hint),
                )
            }
        }
    }
}

/* ---------------------------- Endereços (img_2 / img_3 / img_4) ---------------------------- */

@Composable
private fun EnderecosFlow(onVoltar: () -> Unit, modifier: Modifier = Modifier) {
    val enderecos =
        remember { mutableStateListOf<EnderecoMock>().apply { addAll(ENDERECOS_INICIAIS) } }
    var editando by remember { mutableStateOf<EnderecoMock?>(null) }
    var criando by remember { mutableStateOf(false) }

    BackHandler(enabled = editando != null || criando) {
        editando = null
        criando = false
    }

    when {
        criando -> EditarEnderecoScreen(
            endereco = null,
            titulo = stringResource(R.string.endereco_novo),
            textoBotao = stringResource(R.string.endereco_adicionar),
            onVoltar = { criando = false },
            modifier = modifier,
        )

        editando != null -> EditarEnderecoScreen(
            endereco = editando,
            titulo = stringResource(R.string.endereco_editar_titulo),
            textoBotao = stringResource(R.string.endereco_salvar_alteracoes),
            onVoltar = { editando = null },
            modifier = modifier,
        )

        else -> EnderecosScreen(
            enderecos = enderecos,
            onVoltar = onVoltar,
            onNovo = { criando = true },
            onEditar = { editando = it },
            onExcluir = { alvo -> enderecos.removeAll { it.id == alvo.id } },
            onDefinirPrincipal = { alvo ->
                for (i in enderecos.indices) {
                    enderecos[i] = enderecos[i].copy(principal = enderecos[i].id == alvo.id)
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun EnderecosScreen(
    enderecos: List<EnderecoMock>,
    onVoltar: () -> Unit,
    onNovo: () -> Unit,
    onEditar: (EnderecoMock) -> Unit,
    onExcluir: (EnderecoMock) -> Unit,
    onDefinirPrincipal: (EnderecoMock) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopBarVoltar(
            stringResource(R.string.perfil_enderecos),
            stringResource(R.string.enderecos_sub_salvos),
            onVoltar,
        )
        LazyColumn(
            contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(enderecos, key = { it.id }) { endereco ->
                CardEndereco(
                    endereco = endereco,
                    onEditar = { onEditar(endereco) },
                    onExcluir = { onExcluir(endereco) },
                    onDefinirPrincipal = { onDefinirPrincipal(endereco) },
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .bordaTracejada(MaterialTheme.colorScheme.primary, 14.dp)
                        .clickable(onClick = onNovo)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.endereco_adicionar_novo),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardEndereco(
    endereco: EnderecoMock,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    onDefinirPrincipal: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Row(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(endereco.tipo.cor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        endereco.tipo.icone,
                        null,
                        tint = endereco.tipo.cor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            endereco.nome,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (endereco.principal) {
                            Spacer(Modifier.width(8.dp))
                            ChipPrincipal()
                        }
                    }
                    Text(
                        endereco.logradouro,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        endereco.bairroCidade,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.endereco_cep_format, endereco.cep),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                if (!endereco.principal) {
                    AcaoEndereco(
                        Icons.Outlined.StarBorder, stringResource(R.string.endereco_acao_definir_principal),
                        cor = MaterialTheme.colorScheme.primary, onClick = onDefinirPrincipal,
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outline)
                }
                AcaoEndereco(
                    Icons.Outlined.Edit, stringResource(R.string.endereco_acao_editar),
                    cor = MaterialTheme.colorScheme.onSurfaceVariant, onClick = onEditar,
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider(color = MaterialTheme.colorScheme.outline)
                AcaoEndereco(
                    Icons.Outlined.DeleteOutline, stringResource(R.string.endereco_acao_excluir),
                    cor = MaterialTheme.colorScheme.primary, onClick = onExcluir,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ChipPrincipal() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Icon(
            Icons.Filled.Star,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(11.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            stringResource(R.string.endereco_principal),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AcaoEndereco(
    icone: ImageVector,
    texto: String,
    cor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icone, null, tint = cor, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.labelLarge, color = cor)
    }
}

@Composable
private fun EditarEnderecoScreen(
    endereco: EnderecoMock?,
    titulo: String,
    textoBotao: String,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var nome by rememberSaveable { mutableStateOf(endereco?.nome ?: "") }
    var tipo by rememberSaveable { mutableStateOf(endereco?.tipo ?: TipoEndereco.Casa) }
    var cep by rememberSaveable { mutableStateOf(endereco?.cep ?: "") }
    var rua by rememberSaveable { mutableStateOf(if (endereco != null) "Rua das Flores" else "") }
    var numero by rememberSaveable { mutableStateOf(if (endereco != null) "123" else "") }
    var complemento by rememberSaveable { mutableStateOf(if (endereco != null) "Apto 42" else "") }
    var bairro by rememberSaveable { mutableStateOf(if (endereco != null) "Jardim América" else "") }
    var cidade by rememberSaveable { mutableStateOf(if (endereco != null) "São Paulo" else "") }
    var estado by rememberSaveable { mutableStateOf(if (endereco != null) "SP" else "SP") }

    val podeSalvar = nome.isNotBlank() && cep.isNotBlank() && rua.isNotBlank() &&
            numero.isNotBlank() && bairro.isNotBlank() && cidade.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopBarVoltar(titulo = titulo, null, onVoltar = onVoltar)

        LazyColumn(
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                CampoTexto(
                    stringResource(R.string.endereco_campo_nome), nome, { nome = it },
                    if (endereco == null) stringResource(R.string.endereco_campo_nome_hint_novo)
                    else stringResource(R.string.endereco_campo_nome),
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.endereco_tipo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TipoEndereco.entries.forEach { opcao ->
                            SeletorTipo(
                                tipo = opcao,
                                selecionado = tipo == opcao,
                                onClick = { tipo = opcao },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            item {
                CampoTexto(
                    stringResource(R.string.endereco_cep),
                    cep,
                    { cep = it },
                    stringResource(R.string.endereco_cep_hint),
                    teclado = KeyboardType.Number
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampoTexto(
                        stringResource(R.string.endereco_rua),
                        rua,
                        { rua = it },
                        stringResource(R.string.endereco_rua_hint),
                        modifier = Modifier.weight(1f)
                    )
                    CampoTexto(
                        stringResource(R.string.endereco_numero),
                        numero,
                        { numero = it },
                        stringResource(R.string.endereco_numero_hint),
                        teclado = KeyboardType.Number,
                        modifier = Modifier.width(96.dp)
                    )
                }
            }
            item {
                CampoTexto(
                    stringResource(R.string.endereco_complemento),
                    complemento,
                    { complemento = it },
                    stringResource(R.string.endereco_complemento_hint)
                )
            }
            item {
                CampoTexto(
                    stringResource(R.string.endereco_bairro),
                    bairro,
                    { bairro = it },
                    stringResource(R.string.endereco_bairro),
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampoTexto(
                        stringResource(R.string.endereco_cidade),
                        cidade,
                        { cidade = it },
                        stringResource(R.string.endereco_cidade),
                        modifier = Modifier.weight(1f)
                    )
                    CampoTexto(
                        stringResource(R.string.endereco_estado),
                        estado,
                        { estado = it },
                        stringResource(R.string.endereco_estado_hint),
                        modifier = Modifier.width(88.dp)
                    )
                }
            }
            item {
                Button(
                    onClick = onVoltar,
                    enabled = podeSalvar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(textoBotao, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun SeletorTipo(
    tipo: TipoEndereco,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .bordaSolida(
                if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                14.dp,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            tipo.icone,
            null,
            tint = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            stringResource(tipo.rotuloRes),
            style = MaterialTheme.typography.labelLarge,
            color = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/* ---------------------------- Ajuda (img_5 / img_6) ---------------------------- */

@Composable
private fun AjudaScreen(onVoltar: () -> Unit, modifier: Modifier = Modifier) {
    var categoria by rememberSaveable { mutableStateOf(FAQ_TODOS) }
    val faqsVisiveis = remember(categoria) {
        if (categoria == FAQ_TODOS) FAQS else FAQS.filter { it.categoria == categoria }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopBarVoltar(
            stringResource(R.string.perfil_ajuda),
            stringResource(R.string.ajuda_sub),
            onVoltar,
        )
        LazyColumn(
            contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .bordaSolida(MaterialTheme.colorScheme.outline, 14.dp)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
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
                        stringResource(R.string.ajuda_buscar),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item { RotuloSecaoAjuda(stringResource(R.string.ajuda_falar_suporte)) }
            item {
                CardSuporte(
                    Icons.Outlined.ChatBubbleOutline,
                    Verde,
                    stringResource(R.string.ajuda_chat),
                    stringResource(R.string.ajuda_chat_sub)
                )
            }
            item {
                CardSuporte(
                    Icons.Outlined.Phone,
                    Azul,
                    stringResource(R.string.perfil_telefone),
                    stringResource(R.string.ajuda_telefone_sub)
                )
            }
            item {
                CardSuporte(
                    Icons.Outlined.MailOutline,
                    Roxo,
                    stringResource(R.string.perfil_email),
                    stringResource(R.string.ajuda_email_sub)
                )
            }

            item { RotuloSecaoAjuda(stringResource(R.string.ajuda_atalhos)) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AtalhoRapido(
                        Icons.Outlined.ReportProblem,
                        MaterialTheme.colorScheme.primary,
                        stringResource(R.string.ajuda_reportar),
                        Modifier.weight(1f)
                    )
                    AtalhoRapido(
                        Icons.Outlined.Shield,
                        Verde,
                        stringResource(R.string.ajuda_protecao),
                        Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AtalhoRapido(
                        Icons.Outlined.StarBorder,
                        Amarelo,
                        stringResource(R.string.ajuda_avaliar_app),
                        Modifier.weight(1f)
                    )
                    AtalhoRapido(
                        Icons.Outlined.Description,
                        Azul,
                        stringResource(R.string.ajuda_termos),
                        Modifier.weight(1f)
                    )
                }
            }

            item { RotuloSecaoAjuda(stringResource(R.string.ajuda_faq_titulo)) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FAQ_CATEGORIAS) { cat ->
                        val selecionado = cat == categoria
                        FilterChip(
                            selected = selecionado,
                            onClick = { categoria = cat },
                            label = {
                                Text(
                                    cat,
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
                }
            }

            items(faqsVisiveis, key = { it.pergunta }) { faq ->
                FaqItem(faq)
            }
        }
    }
}

@Composable
private fun RotuloSecaoAjuda(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
    )
}

@Composable
private fun CardSuporte(icone: ImageVector, cor: Color, titulo: String, subtitulo: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, null, tint = cor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
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
        }
    }
}

@Composable
private fun AtalhoRapido(
    icone: ImageVector,
    cor: Color,
    titulo: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, null, tint = cor, modifier = Modifier.size(20.dp))
            }
            Text(
                titulo,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FaqItem(faq: Faq) {
    var expandido by rememberSaveable(faq.pergunta) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    faq.categoria,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    faq.pergunta,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(visible = expandido) {
                Text(
                    stringResource(R.string.ajuda_faq_resposta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                )
            }
        }
    }
}


@Composable
private fun TopBarVoltar(titulo: String, subtitulo: String?, onVoltar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 8.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            stringResource(R.string.voltar),
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
            if (subtitulo != null) {
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CampoTexto(
    label: String,
    valor: String,
    onValor: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValor,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

@Composable
private fun AvatarIniciais(
    nome: String,
    tamanho: Dp,
    fonte: TextStyle,
) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = iniciais(nome),
            style = fonte,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun iniciais(nome: String): String =
    nome.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

/** Borda sólida arredondada sem depender do módulo foundation.border. */
private fun Modifier.bordaSolida(cor: Color, raio: Dp): Modifier = drawBehind {
    drawRoundRect(
        color = cor,
        cornerRadius = CornerRadius(raio.toPx()),
        style = Stroke(width = 1.5.dp.toPx()),
    )
}

/** Borda tracejada arredondada (usada no "Adicionar novo endereço"). */
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
