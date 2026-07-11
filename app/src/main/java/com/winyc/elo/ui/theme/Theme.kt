package com.winyc.elo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Contexto de cor do app. A marca usa a cor como sinal de contexto:
 * coral quando o usuário está no modo cliente, teal no modo profissional.
 * Nas telas neutras (abertura, escolha de perfil, notificações) os dois lados
 * ainda não se separaram — use [Neutro], que adota o coral como cor primária
 * padrão, mas mantém coral e teal acessíveis via [LocalEloColors].
 */
enum class EloContext { Cliente, Profissional, Neutro }

@Immutable
data class EloColors(
    val coral: Color,
    val coralClaro: Color,
    val teal: Color,
    val tealClaro: Color,
    val tintaEscura: Color,
    val avaliacao: Color,
    val textoSuave: Color,
)

val LocalEloColors = staticCompositionLocalOf {
    EloColors(
        coral = EloCoral,
        coralClaro = EloCoralClaro,
        teal = EloTeal,
        tealClaro = EloTealClaro,
        tintaEscura = EloTintaEscura,
        avaliacao = EloAvaliacao,
        textoSuave = EloTextoSuave,
    )
}

object EloTheme {
    val colors: EloColors
        @Composable
        @ReadOnlyComposable
        get() = LocalEloColors.current
}

private fun lightScheme(context: Color, contextContainer: Color) = lightColorScheme(
    primary = context,
    onPrimary = Color.White,
    primaryContainer = contextContainer,
    onPrimaryContainer = EloTexto,
    secondary = EloTextoSuave,
    onSecondary = Color.White,
    secondaryContainer = EloBorda,
    onSecondaryContainer = EloTexto,
    tertiary = EloAvaliacao,
    onTertiary = EloTintaEscura,
    background = EloFundo,
    onBackground = EloTexto,
    surface = EloSuperficie,
    onSurface = EloTexto,
    surfaceVariant = EloFundo,
    onSurfaceVariant = EloTextoSuave,
    outline = EloBorda,
    outlineVariant = EloBorda,
)

private fun darkScheme(context: Color, contextContainer: Color) = darkColorScheme(
    primary = context,
    onPrimary = EloTintaEscuraDark,
    primaryContainer = contextContainer,
    onPrimaryContainer = EloTextoDark,
    secondary = EloTextoSuaveDark,
    onSecondary = EloTintaEscuraDark,
    secondaryContainer = EloBordaDark,
    onSecondaryContainer = EloTextoDark,
    tertiary = EloAvaliacaoDark,
    onTertiary = EloTintaEscuraDark,
    background = EloFundoDark,
    onBackground = EloTextoDark,
    surface = EloSuperficieDark,
    onSurface = EloTextoDark,
    surfaceVariant = EloSuperficieDark,
    onSurfaceVariant = EloTextoSuaveDark,
    outline = EloBordaDark,
    outlineVariant = EloBordaDark,
)

@Composable
fun EloTheme(
    context: EloContext = EloContext.Neutro,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val contextColor: Color
    val contextContainer: Color
    when (context) {
        EloContext.Profissional -> {
            contextColor = if (darkTheme) EloTealDark else EloTeal
            contextContainer = if (darkTheme) EloTealClaroDark else EloTealClaro
        }
        EloContext.Cliente, EloContext.Neutro -> {
            contextColor = if (darkTheme) EloCoralDark else EloCoral
            contextContainer = if (darkTheme) EloCoralClaroDark else EloCoralClaro
        }
    }

    val colorScheme = if (darkTheme) {
        darkScheme(contextColor, contextContainer)
    } else {
        lightScheme(contextColor, contextContainer)
    }

    val eloColors = EloColors(
        coral = if (darkTheme) EloCoralDark else EloCoral,
        coralClaro = if (darkTheme) EloCoralClaroDark else EloCoralClaro,
        teal = if (darkTheme) EloTealDark else EloTeal,
        tealClaro = if (darkTheme) EloTealClaroDark else EloTealClaro,
        tintaEscura = if (darkTheme) EloTintaEscuraDark else EloTintaEscura,
        avaliacao = if (darkTheme) EloAvaliacaoDark else EloAvaliacao,
        textoSuave = if (darkTheme) EloTextoSuaveDark else EloTextoSuave,
    )

    CompositionLocalProvider(LocalEloColors provides eloColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}