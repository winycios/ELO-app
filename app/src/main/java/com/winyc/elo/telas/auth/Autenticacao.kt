package com.winyc.elo.telas.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.winyc.elo.R
import com.winyc.elo.backend.viewModel.AuthEstado
import com.winyc.elo.backend.viewModel.AuthViewModel
import com.winyc.elo.telas.componentes.ToastAviso
import com.winyc.elo.ui.theme.EloContext
import com.winyc.elo.ui.theme.EloTheme
import com.winyc.elo.ui.theme.EloTintaEscura
import kotlinx.coroutines.delay

private enum class ModoAuth { Entrar, Cadastrar }

private enum class PerfilCadastro { Cliente, Profissional }

private data class Estatistica(val icone: ImageVector, val valor: String, val rotulo: String)

private val ESTATISTICAS = listOf(
    Estatistica(Icons.Outlined.People, "+2 milhões", "de clientes atendidos"),
    Estatistica(Icons.Filled.Star, "4.9", "avaliação média"),
    Estatistica(Icons.Outlined.Verified, "+80 mil", "profissionais verificados"),
)

@Composable
fun AutenticacaoScreen(
    onSair: () -> Unit,
    onAutenticar: () -> Unit = onSair,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
) {
    var modo by rememberSaveable { mutableStateOf(ModoAuth.Entrar) }
    var perfil by rememberSaveable { mutableStateOf(PerfilCadastro.Cliente) }

    val estado by authViewModel.estado.collectAsStateWithLifecycle()
    val carregando = estado is AuthEstado.Carregando
    val erro = (estado as? AuthEstado.Erro)?.mensagem

    LaunchedEffect(estado) {
        if (estado is AuthEstado.Sucesso) onAutenticar()
    }

    val contexto = if (modo == ModoAuth.Cadastrar && perfil == PerfilCadastro.Profissional) {
        EloContext.Profissional
    } else {
        EloContext.Cliente
    }

    // Enquanto houver erro, mostra o toast e o descarta sozinho após alguns segundos.
    LaunchedEffect(erro) {
        if (!erro.isNullOrBlank()) {
            delay(4000)
            authViewModel.limparErro()
        }
    }

    EloTheme(context = contexto) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding(),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Cabecalho(modo = modo, onPular = onSair)
                    SeletorModo(
                        modo = modo,
                        onModo = { modo = it },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 20.dp)
                            .offset(y = 30.dp),
                    )
                }

                Spacer(Modifier.height(46.dp))

                AnimatedContent(
                    targetState = modo,
                    transitionSpec = {
                        val paraCadastro = targetState == ModoAuth.Cadastrar
                        val dir = if (paraCadastro) 1 else -1
                        (slideInHorizontally(tween(280)) { largura -> dir * largura / 3 } + fadeIn(
                            tween(280)
                        ))
                            .togetherWith(
                                slideOutHorizontally(tween(220)) { largura -> -dir * largura / 3 } + fadeOut(
                                    tween(180)
                                ),
                            )
                    },
                    label = "transicao-auth",
                ) { alvo ->
                    when (alvo) {
                        ModoAuth.Entrar -> FormularioEntrar(
                            carregando = carregando,
                            onEntrar = { email, senha -> authViewModel.login(email, senha) },
                            onIrParaCadastro = {
                                authViewModel.limparErro()
                                modo = ModoAuth.Cadastrar
                            },
                        )

                        ModoAuth.Cadastrar -> FormularioCadastro(
                            perfil = perfil,
                            carregando = carregando,
                            onPerfil = { perfil = it },
                            onCriarConta = { nome, sobrenome, telefone, email, senha ->
                                authViewModel.cadastrar(
                                    nome = nome,
                                    sobrenome = sobrenome,
                                    telefone = telefone,
                                    email = email,
                                    senha = senha,
                                    comoProfissional = perfil == PerfilCadastro.Profissional,
                                )
                            },
                            onIrParaLogin = {
                                authViewModel.limparErro()
                                modo = ModoAuth.Entrar
                            },
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            ToastAviso(
                mensagem = erro,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
            )
        }
    }
}


@Composable
private fun Cabecalho(modo: ModoAuth, onPular: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(MaterialTheme.colorScheme.primary),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-50).dp)
                .size(190.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
        )

        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 44.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EloTintaEscura),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo Elo",
                        modifier = Modifier.size(72.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "E L O",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onPular)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Pular",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = if (modo == ModoAuth.Entrar) "Bem-vindo de volta" else "Crie sua conta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = if (modo == ModoAuth.Entrar) {
                    "Entre para contratar profissionais de confiança"
                } else {
                    "Junte-se à maior comunidade de serviços do Brasil"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ESTATISTICAS.forEach { estatistica ->
                    CartaoEstatistica(estatistica, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CartaoEstatistica(estatistica: Estatistica, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            estatistica.icone,
            null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = estatistica.valor,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Text(
            text = estatistica.rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun SeletorModo(modo: ModoAuth, onModo: (ModoAuth) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SegmentoModo(
            "Entrar",
            selecionado = modo == ModoAuth.Entrar,
            onClick = { onModo(ModoAuth.Entrar) },
            modifier = Modifier.weight(1f)
        )
        SegmentoModo(
            "Cadastrar",
            selecionado = modo == ModoAuth.Cadastrar,
            onClick = { onModo(ModoAuth.Cadastrar) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentoModo(
    texto: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selecionado) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleSmall,
            color = if (selecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/* ---------------------------- Formulário: Entrar ---------------------------- */

@Composable
private fun FormularioEntrar(
    carregando: Boolean,
    onEntrar: (email: String, senha: String) -> Unit,
    onIrParaCadastro: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CampoAuth(
            valor = email,
            onValor = { email = it },
            placeholder = "E-mail",
            leading = Icons.Outlined.MailOutline,
            tipoTeclado = KeyboardType.Email,
        )
        CampoAuth(
            valor = senha,
            onValor = { senha = it },
            placeholder = "Senha",
            leading = Icons.Outlined.Lock,
            senha = true,
        )
        Text(
            text = "Esqueceu a senha?",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(6.dp))
                .clickable { }
                .padding(4.dp),
        )
        BotaoPrincipal(
            texto = "Entrar",
            habilitado = email.isNotBlank() && senha.isNotBlank() && !carregando,
            carregando = carregando,
            onClick = { onEntrar(email, senha) },
        )
        DivisorOu()
        BotaoGoogle()
        SeloSeguranca()
        TrocarModo(
            pergunta = "Ainda não tem conta?",
            acao = "Cadastre-se",
            onClick = onIrParaCadastro,
        )
    }
}

/* ---------------------------- Formulário: Cadastrar ---------------------------- */

@Composable
private fun FormularioCadastro(
    perfil: PerfilCadastro,
    carregando: Boolean,
    onPerfil: (PerfilCadastro) -> Unit,
    onCriarConta: (nome: String, sobrenome: String, telefone: String, email: String, senha: String) -> Unit,
    onIrParaLogin: () -> Unit,
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var sobrenome by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var aceitou by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Quero me cadastrar como",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CartaoPerfil(
                titulo = "Cliente",
                subtitulo = "Contratar serviços",
                icone = Icons.Outlined.PersonOutline,
                selecionado = perfil == PerfilCadastro.Cliente,
                onClick = { onPerfil(PerfilCadastro.Cliente) },
                modifier = Modifier.weight(1f),
            )
            CartaoPerfil(
                titulo = "Profissional",
                subtitulo = "Oferecer serviços",
                icone = Icons.Outlined.WorkOutline,
                selecionado = perfil == PerfilCadastro.Profissional,
                onClick = { onPerfil(PerfilCadastro.Profissional) },
                modifier = Modifier.weight(1f),
            )
        }



        CampoAuth(nome, { nome = it }, "Nome", Icons.Outlined.PersonOutline)
        CampoAuth(sobrenome, { sobrenome = it }, "Sobrenome", Icons.Outlined.PersonOutline)

        CampoAuth(
            telefone,
            { telefone = it },
            "Telefone",
            Icons.Outlined.Phone,
            tipoTeclado = KeyboardType.Phone,
        )
        CampoAuth(
            email,
            { email = it },
            "E-mail",
            Icons.Outlined.MailOutline,
            tipoTeclado = KeyboardType.Email
        )
        CampoAuth(senha, { senha = it }, "Senha", Icons.Outlined.Lock, senha = true)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = aceitou,
                onCheckedChange = { aceitou = it },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
            )
            Text(
                text = buildAnnotatedString {
                    append("Li e aceito os ")
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append("Termos de Uso")
                    }
                    append(" e a ")
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append("Política de Privacidade")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        BotaoPrincipal(
            texto = "Criar conta",
            habilitado = nome.isNotBlank() && nome.length >= 3 && sobrenome.isNotBlank() && telefone.isNotBlank() &&
                    email.isNotBlank() && senha.isNotBlank() && aceitou && !carregando,
            carregando = carregando,
            onClick = { onCriarConta(nome, sobrenome, telefone, email, senha) },
        )
        DivisorOu()
        BotaoGoogle()
        SeloSeguranca()
        TrocarModo(
            pergunta = "Já tem uma conta?",
            acao = "Entrar",
            onClick = onIrParaLogin,
        )
    }
}

@Composable
private fun CartaoPerfil(
    titulo: String,
    subtitulo: String,
    icone: ImageVector,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(
                width = 1.5.dp,
                color = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                icone,
                null,
                tint = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                color = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selecionado) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Check,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun CampoAuth(
    valor: String,
    onValor: (String) -> Unit,
    placeholder: String,
    leading: ImageVector,
    senha: Boolean = false,
    tamanho: Float = 1f,
    tipoTeclado: KeyboardType = KeyboardType.Text,
) {
    var visivel by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        modifier = Modifier.fillMaxWidth(tamanho),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                leading,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (senha) {
            {
                IconButton(onClick = { visivel = !visivel }) {
                    Icon(
                        imageVector = if (visivel) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (visivel) "Ocultar senha" else "Mostrar senha",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            null
        },
        visualTransformation = if (senha && !visivel) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@Composable
private fun BotaoPrincipal(
    texto: String,
    habilitado: Boolean,
    onClick: () -> Unit,
    carregando: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
        ),
    ) {
        if (carregando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(texto, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
        }
    }
}


@Composable
private fun BotaoGoogle() {
    OutlinedButton(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Text(
            text = "Google",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DivisorOu() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
        Text(
            text = "ou continue com",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun SeloSeguranca() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Shield,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Seus dados estão protegidos e nunca são compartilhados",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TrocarModo(pergunta: String, acao: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = pergunta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = acao,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}