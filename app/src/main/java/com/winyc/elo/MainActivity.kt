package com.winyc.elo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.net.Uri
import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.winyc.elo.backend.security.TokenStore
import com.winyc.elo.telas.auth.AutenticacaoScreen
import com.winyc.elo.telas.cliente.InicioScreen
import com.winyc.elo.telas.onboarding.OnboardingScreen
import com.winyc.elo.telas.cliente.PedidosScreen
import com.winyc.elo.telas.cliente.PerfilProfissionalScreen
import com.winyc.elo.telas.cliente.PerfilScreen
import com.winyc.elo.telas.cliente.VitrineScreen
import com.winyc.elo.telas.profissional.OrcamentosScreen
import com.winyc.elo.telas.profissional.PainelScreen
import com.winyc.elo.telas.profissional.PerfilProScreen
import com.winyc.elo.telas.profissional.PublicarScreen
import com.winyc.elo.ui.theme.EloContext
import com.winyc.elo.ui.theme.EloTheme
import androidx.core.content.edit
import kotlinx.coroutines.launch


private const val PRO_PREFIX = "pro/"
private const val PRO_PERFIL_PREFIX = "cliente/profissional/"
private const val PREFS = "elo_prefs"
private const val KEY_ONBOARDING = "onboarding_visto"

private enum class EloScreen(val route: String) {
    // Cliente (coral)
    Inicio("cliente/inicio"),
    Vitrine("cliente/vitrine"),
    Pedidos("cliente/pedidos"),
    Perfil("cliente/perfil"),
    PerfilProfissional("cliente/profissional/{nome}"),

    // Profissional (teal)
    Painel("${PRO_PREFIX}painel"),
    Orcamentos("${PRO_PREFIX}orcamentos"),
    Publicar("${PRO_PREFIX}publicar"),
    PerfilPro("${PRO_PREFIX}perfil"),

    Onboarding("onboarding"),
    Auth("auth"),
}

private data class NavItem(
    val screen: EloScreen,
    val labelRes: Int,
    val icon: ImageVector,
)

private val CLIENTE_ITENS = listOf(
    NavItem(EloScreen.Inicio, R.string.inicio, Icons.Outlined.Home),
    NavItem(EloScreen.Vitrine, R.string.vitrine, Icons.Outlined.Storefront),
    NavItem(EloScreen.Pedidos, R.string.pedidos, Icons.AutoMirrored.Outlined.ReceiptLong),
    NavItem(EloScreen.Perfil, R.string.perfil, Icons.Outlined.Person),
)

private val PROFISSIONAL_ITENS = listOf(
    NavItem(EloScreen.Painel, R.string.painel, Icons.Outlined.GridView),
    NavItem(EloScreen.Orcamentos, R.string.orcamentos, Icons.Outlined.Description),
    NavItem(EloScreen.Publicar, R.string.publicar, Icons.Outlined.AddBox),
    NavItem(EloScreen.PerfilPro, R.string.perfil, Icons.Outlined.ManageAccounts),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val tokenStore = TokenStore.getInstance(this)
        splashScreen.setKeepOnScreenCondition { !tokenStore.carregada }
        lifecycleScope.launch { tokenStore.carregar() }
        enableEdgeToEdge()
        setContent {
            EloApp()
        }
    }
}

@Composable
private fun EloApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val appContext = LocalContext.current
    val prefs = remember { appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    val jaViuOnboarding = remember { prefs.getBoolean(KEY_ONBOARDING, false) }

    val escopo = rememberCoroutineScope()
    val tokenStore = remember { TokenStore.getInstance(appContext) }
    val carregada by tokenStore.carregadaFlow.collectAsStateWithLifecycle()
    val logado by tokenStore.estaLogadoFlow.collectAsStateWithLifecycle()
    val perfil by tokenStore.perfilFlow.collectAsStateWithLifecycle()
    
    if (!carregada) return

    val soProfissional = logado && perfil?.profissionalAtivo == true && perfil?.clienteAtivo != true
    val startDestination = when {
        !jaViuOnboarding -> EloScreen.Onboarding.route
        soProfissional -> EloScreen.Painel.route
        else -> EloScreen.Inicio.route
    }

    val emModoPro = currentRoute?.startsWith(PRO_PREFIX) == true
    val telaCheia = currentRoute == EloScreen.Auth.route ||
        currentRoute == EloScreen.Onboarding.route ||
        currentRoute == EloScreen.PerfilProfissional.route
    val context = if (emModoPro) EloContext.Profissional else EloContext.Cliente

    val podePro = logado && perfil?.profissionalAtivo == true
    val podeCliente = !logado || perfil?.clienteAtivo == true
    val toggleBloqueado = if (emModoPro) !podeCliente else !podePro

    LaunchedEffect(soProfissional, logado, emModoPro, currentRoute, telaCheia) {
        if (currentRoute == null || telaCheia) return@LaunchedEffect
        when {
            soProfissional && !emModoPro -> navController.trocarDeModo(EloScreen.Painel)
            emModoPro && !logado -> navController.trocarDeModo(EloScreen.Inicio)
        }
    }

    var balaoDispensado by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(logado) { if (logado) balaoDispensado = false }

    val snackbarHostState = remember { SnackbarHostState() }
    val msgBloqueio = stringResource(
        if (emModoPro) R.string.modo_bloqueado_cliente else R.string.modo_bloqueado_pro,
    )

    EloTheme(context = context) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (emModoPro && !telaCheia) ModoProBanner()
            },
            bottomBar = {
                if (!telaCheia) {
                    Column {
                        if (!logado && !balaoDispensado) {
                            BalaoDeslogado(
                                onEntrar = { navController.navigate(EloScreen.Auth.route) },
                                onDispensar = { balaoDispensado = true },
                            )
                        }
                        EloNavigationBar(
                            itens = if (emModoPro) PROFISSIONAL_ITENS else CLIENTE_ITENS,
                            currentRoute = currentRoute,
                            onNavigate = { navController.navegarParaAba(it) },
                            toggleLabelRes = if (emModoPro) R.string.cliente else R.string.profissional,
                            toggleIcon = if (emModoPro) Icons.Outlined.Person else Icons.Outlined.WorkOutline,
                            toggleBloqueado = toggleBloqueado,
                            onToggle = {
                                if (toggleBloqueado) {
                                    escopo.launch { snackbarHostState.showSnackbar(msgBloqueio) }
                                } else {
                                    val destino = if (emModoPro) EloScreen.Inicio else EloScreen.Painel
                                    navController.trocarDeModo(destino)
                                }
                            },
                        )
                    }
                }
            },
        ) { contentPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .padding(contentPadding)
                    .then(if (telaCheia) Modifier else Modifier.padding(start = 10.dp, end = 10.dp)),

                enterTransition = {
                    slideInHorizontally(animationSpec = tween(300)) { it / 3 } + fadeIn(tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(250)) { -it / 3 } + fadeOut(tween(200))
                },
                popEnterTransition = {
                    slideInHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeIn(tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(250)) { it / 3 } + fadeOut(tween(200))
                },
            ) {
                composable(EloScreen.Inicio.route) {
                    InicioScreen(onAbrirPerfil = { navController.abrirPerfilProfissional(it)}, perfil = perfil)
                }
                composable(EloScreen.Vitrine.route) {
                    VitrineScreen(
                        logado = logado,
                        onAbrirPerfil = { navController.abrirPerfilProfissional(it) },
                        onPrecisaLogin = { navController.navigate(EloScreen.Auth.route) },
                    )
                }
                composable(EloScreen.Pedidos.route) { PedidosScreen() }
                composable(EloScreen.Perfil.route) {
                    PerfilScreen(
                        logado = logado,
                        perfil = perfil,
                        onAbrirLogin = { navController.navigate(EloScreen.Auth.route) },
                        onSair = { escopo.launch { tokenStore.limpar() } },
                    )
                }
                composable(
                    EloScreen.PerfilProfissional.route,
                    arguments = listOf(navArgument("nome") { type = NavType.StringType }),
                ) { entry ->
                    val nome = entry.arguments?.getString("nome").orEmpty()
                    PerfilProfissionalScreen(
                        nome = nome,
                        logado = logado,
                        onPrecisaLogin = { navController.navigate(EloScreen.Auth.route) },
                        onVoltar = { navController.popBackStack() },
                        onIrParaInicio = { navController.navegarParaAba(EloScreen.Inicio) },
                        onVerPedidos = { navController.navegarParaAba(EloScreen.Pedidos) },
                    )
                }

                composable(EloScreen.Painel.route) { PainelScreen() }
                composable(EloScreen.Orcamentos.route) { OrcamentosScreen() }
                composable(EloScreen.Publicar.route) { PublicarScreen() }
                composable(EloScreen.PerfilPro.route) {
                    PerfilProScreen(
                        sessao = perfil,
                        onSair = { escopo.launch { tokenStore.limpar() } },
                    )
                }

                composable(EloScreen.Onboarding.route) {
                    OnboardingScreen(
                        onConcluir = {
                            prefs.edit { putBoolean(KEY_ONBOARDING, true) }
                            navController.navigate(EloScreen.Inicio.route) {
                                popUpTo(EloScreen.Onboarding.route) { inclusive = true }
                            }
                        },
                    )
                }

                composable(EloScreen.Auth.route) {
                    AutenticacaoScreen(
                        onSair = { navController.popBackStack() },
                        onAutenticar = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

private fun NavController.navegarParaAba(screen: EloScreen) {
    navigate(screen.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavController.abrirPerfilProfissional(nome: String) {
    navigate("${PRO_PERFIL_PREFIX}${Uri.encode(nome)}") {
        launchSingleTop = true
    }
}

private fun NavController.trocarDeModo(destino: EloScreen) {
    navigate(destino.route) {
        popUpTo(graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun ModoProBanner() {
    Surface(color = MaterialTheme.colorScheme.primary) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.WorkOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.modo_profissional_ativo),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/** Aviso fixo (acima da barra) para o usuário deslogado, com atalho para entrar. */
@Composable
private fun BalaoDeslogado(onEntrar: () -> Unit, onDispensar: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.deslogado_titulo),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(R.string.deslogado_texto),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                )
            }
            Button(onClick = onEntrar) {
                Text(stringResource(R.string.deslogado_entrar))
            }
            IconButton(onClick = onDispensar) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.deslogado_dispensar_cd),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun EloNavigationBar(
    itens: List<NavItem>,
    currentRoute: String?,
    onNavigate: (EloScreen) -> Unit,
    toggleLabelRes: Int,
    toggleIcon: ImageVector,
    onToggle: () -> Unit,
    toggleBloqueado: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val corContexto = MaterialTheme.colorScheme.primary
    val corInativa = MaterialTheme.colorScheme.onSurfaceVariant
    val corToggle = if (toggleBloqueado) corInativa else corContexto

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        NavigationBar(
            modifier = Modifier.height(70.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            itens.forEach { item ->
                val selecionado = currentRoute == item.screen.route
                val label = stringResource(item.labelRes)
                NavigationBarItem(
                    selected = selecionado,
                    onClick = { onNavigate(item.screen) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = label,
                            modifier = Modifier.height(23.dp),
                        )
                    },
                    label = { NavLabel(label, selecionado) },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = corContexto,
                        selectedTextColor = corContexto,
                        unselectedIconColor = corInativa,
                        unselectedTextColor = corInativa,
                        indicatorColor = Color.Transparent,
                    ),
                )
            }

            val toggleLabel = stringResource(toggleLabelRes)
            NavigationBarItem(
                selected = false,
                onClick = onToggle,
                icon = {
                    Icon(
                        imageVector = if (toggleBloqueado) Icons.Outlined.Lock else toggleIcon,
                        contentDescription = toggleLabel,
                        modifier = Modifier.height(23.dp),
                    )
                },
                label = { NavLabel(toggleLabel, selecionado = true) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = corToggle,
                    selectedTextColor = corToggle,
                    unselectedIconColor = corToggle,
                    unselectedTextColor = corToggle,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
private fun NavLabel(text: String, selecionado: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (selecionado) FontWeight.Medium else FontWeight.Normal,
        fontSize = 10.sp,
        maxLines = 1,
    )
}

@Preview(showBackground = true)
@Composable
private fun EloAppPreview() {
    EloApp()
}
