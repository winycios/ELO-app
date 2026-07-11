package com.winyc.elo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.winyc.elo.ui.theme.EloContext
import com.winyc.elo.ui.theme.EloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instala a splash da marca antes de super.onCreate (tinta escura + anéis).
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EloTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Showcase(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/** Amostra rápida da identidade: tipografia + as duas cores de contexto. */
@Composable
fun Showcase(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("elo", style = MaterialTheme.typography.displaySmall)
        Text(
            "Títulos em Plus Jakarta Sans, corpo em Inter.",
            style = MaterialTheme.typography.bodyMedium,
            color = EloTheme.colors.textoSuave,
        )

        ContextCard(
            context = EloContext.Cliente,
            titulo = "Modo cliente",
            descricao = "Coral — quente, convida a contratar.",
        )
        ContextCard(
            context = EloContext.Profissional,
            titulo = "Modo profissional",
            descricao = "Teal — estável, ligado a trabalho e renda.",
        )
    }
}

@Composable
private fun ContextCard(context: EloContext, titulo: String, descricao: String) {
    // Reaplica o tema no contexto certo: a cor primária muda para coral/teal.
    EloTheme(context = context) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(descricao, style = MaterialTheme.typography.bodyMedium)
                AssistChip(
                    onClick = {},
                    label = { Text("Selecionado", fontWeight = FontWeight.Medium) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
                Button(onClick = {}) { Text("Continuar") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShowcasePreview() {
    EloTheme {
        Showcase()
    }
}