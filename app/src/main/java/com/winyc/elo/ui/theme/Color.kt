package com.winyc.elo.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de marca do Elo.
 *
 * O sistema tem DUAS cores de contexto (coral = cliente, teal = profissional)
 * sobre uma base neutra compartilhada, garantindo que os dois lados pareçam
 * o mesmo produto. Cada token tem uma versão para o modo claro e outra para o
 * modo escuro. Não use esses valores direto na UI: acesse via MaterialTheme /
 * LocalEloColors, que resolvem contexto e tema automaticamente.
 */

// --- Marca: tinta escura (fundo do logo, splash e telas neutras) ---
val EloTintaEscura = Color(0xFF16181F)
val EloTintaEscuraDark = Color(0xFF0F1014)

// --- Contexto: Cliente (coral — quente, convida a contratar) ---
val EloCoral = Color(0xFFF2603E)
val EloCoralDark = Color(0xFFFF7D5C)
val EloCoralClaro = Color(0xFFFDECE8) // fundo de destaque / chips / estados selecionados
val EloCoralClaroDark = Color(0xFF3A2A26)

// --- Contexto: Profissional (teal — estável, ligado a trabalho e renda) ---
val EloTeal = Color(0xFF12A788)
val EloTealDark = Color(0xFF2EC9A8)
val EloTealClaro = Color(0xFFE2F5F0)
val EloTealClaroDark = Color(0xFF173A34)

// --- Neutros (idênticos aos dois lados; é o que unifica o produto) ---
val EloTexto = Color(0xFF16181F)
val EloTextoDark = Color(0xFFF6F7F9)
val EloTextoSuave = Color(0xFF6B7280)
val EloTextoSuaveDark = Color(0xFF9CA3AF)
val EloFundo = Color(0xFFF6F7F9)
val EloFundoDark = Color(0xFF121317)
val EloSuperficie = Color(0xFFFFFFFF)
val EloSuperficieDark = Color(0xFF1E2028)
val EloBorda = Color(0xFFE5E7EB)
val EloBordaDark = Color(0xFF2C2E38)

// --- Semântico: avaliação (estrelas — igual para ambos os lados) ---
val EloAvaliacao = Color(0xFFFFB020)
val EloAvaliacaoDark = Color(0xFFFFC24D)