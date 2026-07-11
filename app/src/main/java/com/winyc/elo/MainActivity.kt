package com.winyc.elo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.winyc.elo.telas.cliente.InicioScreen
import com.winyc.elo.telas.cliente.PedidosScreen
import com.winyc.elo.telas.cliente.PerfilScreen
import com.winyc.elo.telas.cliente.VitrineScreen
import com.winyc.elo.telas.profissional.OrcamentosScreen
import com.winyc.elo.telas.profissional.PainelScreen
import com.winyc.elo.telas.profissional.PerfilProScreen
import com.winyc.elo.telas.profissional.PublicarScreen
import com.winyc.elo.ui.theme.EloContext
import com.winyc.elo.ui.theme.EloTheme


private const val PRO_PREFIX = "pro/"

private enum class EloScreen(val route: String) {
    // Cliente (coral)
    Inicio("cliente/inicio"),
    Vitrine("cliente/vitrine"),
    Pedidos("cliente/pedidos"),
    Perfil("cliente/perfil"),

    // Profissional (teal)
    Painel("${PRO_PREFIX}painel"),
    Orcamentos("${PRO_PREFIX}orcamentos"),
    Publicar("${PRO_PREFIX}publicar"),
    PerfilPro("${PRO_PREFIX}perfil"),
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
    NavItem(EloScreen.PerfilPro, R.string.perfil_pro, Icons.Outlined.ManageAccounts),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
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

    val emModoPro = currentRoute?.startsWith(PRO_PREFIX) == true
    val context = if (emModoPro) EloContext.Profissional else EloContext.Cliente

    EloTheme(context = context) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (emModoPro) ModoProBanner()
            },
            bottomBar = {
                EloNavigationBar(
                    itens = if (emModoPro) PROFISSIONAL_ITENS else CLIENTE_ITENS,
                    currentRoute = currentRoute,
                    onNavigate = { navController.navegarParaAba(it) },
                    toggleLabelRes = if (emModoPro) R.string.cliente else R.string.profissional,
                    toggleIcon = if (emModoPro) Icons.Outlined.Person else Icons.Outlined.WorkOutline,
                    onToggle = {
                        val destino = if (emModoPro) EloScreen.Inicio else EloScreen.Painel
                        navController.trocarDeModo(destino)
                    },
                )
            },
        ) { contentPadding ->
            NavHost(
                navController = navController,
                startDestination = EloScreen.Inicio.route,
                modifier = Modifier.padding(contentPadding).padding(start = 10.dp, end = 10.dp),
            ) {
                composable(EloScreen.Inicio.route) { InicioScreen() }
                composable(EloScreen.Vitrine.route) { VitrineScreen() }
                composable(EloScreen.Pedidos.route) { PedidosScreen() }
                composable(EloScreen.Perfil.route) { PerfilScreen() }

                composable(EloScreen.Painel.route) { PainelScreen() }
                composable(EloScreen.Orcamentos.route) { OrcamentosScreen() }
                composable(EloScreen.Publicar.route) { PublicarScreen() }
                composable(EloScreen.PerfilPro.route) { PerfilProScreen() }
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

@Composable
private fun EloNavigationBar(
    itens: List<NavItem>,
    currentRoute: String?,
    onNavigate: (EloScreen) -> Unit,
    toggleLabelRes: Int,
    toggleIcon: ImageVector,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val corContexto = MaterialTheme.colorScheme.primary
    val corInativa = MaterialTheme.colorScheme.onSurfaceVariant

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
                        imageVector = toggleIcon,
                        contentDescription = toggleLabel,
                        modifier = Modifier.height(23.dp),
                    )
                },
                label = { NavLabel(toggleLabel, selecionado = true) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = corContexto,
                    selectedTextColor = corContexto,
                    unselectedIconColor = corContexto,
                    unselectedTextColor = corContexto,
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
