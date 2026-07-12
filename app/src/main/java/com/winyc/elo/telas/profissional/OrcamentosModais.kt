package com.winyc.elo.telas.profissional

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.winyc.elo.R
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val Verde = Color(0xFF12A15A)
private val Ambar = Color(0xFFDD8A15)

@OptIn(ExperimentalMaterial3Api::class)
private fun esconderEntao(scope: CoroutineScope, sheetState: SheetState, aoFim: () -> Unit) {
    scope.launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) aoFim()
    }
}

/* ------------------------------------------------------------------ */
/* Modal: Enviar orçamento final                                      */
/* ------------------------------------------------------------------ */

/** Um item de custo editável do orçamento. */
private data class ItemCusto(val nome: String, val icone: ImageVector, val valor: String = "")

/** Categorias pré-definidas que viram itens ao serem tocadas. */
private data class Categoria(val nome: String, val icone: ImageVector)

private val CATEGORIAS = listOf(
    Categoria("Mão de obra", Icons.Outlined.Build),
    Categoria("Material", Icons.Outlined.Inventory2),
    Categoria("Deslocamento", Icons.Outlined.NearMe),
)

/** Converte o texto digitado (ex: "1.500,50") em número. */
private fun paraNumero(texto: String): Double {
    val limpo = texto.replace(".", "").replace(",", ".").filter { it.isDigit() || it == '.' }
    return limpo.toDoubleOrNull() ?: 0.0
}

private fun formatarBRL(valor: Double): String =
    "R$ %,.2f".format(Locale.forLanguageTag("pt-BR"), valor)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EnviarOrcamentoSheet(
    orc: Orcamento,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val itens = remember { mutableStateListOf<ItemCusto>() }
    var itemPersonalizado by rememberSaveable { mutableStateOf("") }
    var observacoes by rememberSaveable { mutableStateOf("") }

    val total = itens.sumOf { paraNumero(it.valor) }

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.92f).padding(vertical = 20.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                CabecalhoSheet(
                    titulo = "Enviar orçamento final",
                    subtitulo = "Detalhe os custos para o cliente",
                    onFechar = { esconderEntao(scope, sheetState, onFechar) },
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Spacer(Modifier.size(2.dp))
                // Cartão do cliente.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarCliente(orc.cliente, tamanho = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            orc.cliente,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            orc.servico,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SecaoLabel("Fotos do cliente (3)")
                GaleriaFotos()

                // Cabeçalho da seção com contador de itens.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SecaoLabel("Detalhamento de custos", icone = Icons.Outlined.Paid)
                    if (itens.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${itens.size} ${if (itens.size == 1) "item" else "itens"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }

                Text(
                    stringResource(id = R.string.adicionar_categoria),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CATEGORIAS.forEach { cat ->
                        val ativa = itens.any { it.nome == cat.nome }
                        ChipCategoria(
                            icone = cat.icone,
                            texto = cat.nome,
                            ativa = ativa,
                            onClick = { itens.add(ItemCusto(cat.nome, cat.icone)) },
                        )
                    }
                }

                // Lista de itens editáveis.
                if (itens.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(12.dp)
                            ),
                    ) {
                        itens.forEachIndexed { i, item ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            ItemCustoRow(
                                item = item,
                                onValor = { novo -> itens[i] = item.copy(valor = novo) },
                                onRemover = { itens.removeAt(i) },
                            )
                        }
                    }
                }

                // Item personalizado.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = itemPersonalizado,
                        onValueChange = { itemPersonalizado = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Item personalizado (ex: Taxa de urgência)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (itemPersonalizado.isNotBlank()) {
                                itens.add(ItemCusto(itemPersonalizado.trim(), Icons.Outlined.Sell))
                                itemPersonalizado = ""
                            }
                        },
                        enabled = itemPersonalizado.isNotBlank(),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Adicionar item")
                    }
                }

                // Resumo com total calculado automaticamente.
                if (itens.any { paraNumero(it.valor) > 0 }) {
                    ResumoOrcamento(itens = itens, total = total)
                }

                SecaoLabel("Observações (opcional)", icone = Icons.Outlined.StickyNote2)
                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = { Text("Garantia, prazo de conclusão, condições de pagamento…") },
                    shape = RoundedCornerShape(12.dp),
                )

                BannerDica("Orçamentos detalhados por categoria geram 42% mais conversões — o cliente entende exatamente pelo que está pagando.")
                Spacer(Modifier.size(2.dp))
            }

            // Botão fixo embaixo, mostrando o total.
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 16.dp)
                    .navigationBarsPadding(),
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Orçamento enviado!", Toast.LENGTH_SHORT).show()
                        esconderEntao(scope, sheetState, onFechar)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = total > 0,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (total > 0) "${stringResource(R.string.enviar_orcamento)} · ${
                            formatarBRL(
                                total
                            )
                        }"
                        else stringResource(R.string.enviar_orcamento),
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemCustoRow(item: ItemCusto, onValor: (String) -> Unit, onRemover: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            item.icone,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            item.nome,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            "R$",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(6.dp))
        OutlinedTextField(
            value = item.valor,
            onValueChange = onValor,
            modifier = Modifier.width(96.dp),
            placeholder = { Text("0") },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        IconButton(onClick = onRemover) {
            Icon(
                Icons.Outlined.DeleteOutline,
                contentDescription = "Remover item",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ResumoOrcamento(itens: List<ItemCusto>, total: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
    ) {
        itens.filter { paraNumero(it.valor) > 0 }.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    item.nome,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatarBRL(paraNumero(item.valor)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Verde.copy(alpha = 0.10f))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Paid, null, tint = Verde, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Total do orçamento",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                formatarBRL(total),
                style = MaterialTheme.typography.titleMedium,
                color = Verde,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GaleriaFotos() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(40.dp),
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(8.dp),
        ) {
            SetaFoto(Icons.AutoMirrored.Filled.KeyboardArrowLeft)
        }
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(8.dp),
        ) {
            SetaFoto(Icons.AutoMirrored.Filled.KeyboardArrowRight)
        }
        Text(
            text = "1/3",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun SetaFoto(icone: ImageVector) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ChipCategoria(
    icone: ImageVector,
    texto: String,
    ativa: Boolean,
    onClick: () -> Unit,
) {
    val cor = if (ativa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .then(
                if (ativa) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icone, null, tint = cor, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.labelMedium, color = cor)
    }
}

@Composable
private fun BannerDica(texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
    ) {
        Icon(
            Icons.Outlined.AutoAwesome,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/* ------------------------------------------------------------------ */
/* Modal: Detalhes do serviço                                         */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetalhesServicoSheet(
    orc: Orcamento,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onFechar, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxHeight(0.92f)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                CabecalhoSheet(
                    titulo = "Detalhes do serviço",
                    subtitulo = orc.servico,
                    onFechar = { esconderEntao(scope, sheetState, onFechar) },
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.size(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarCliente(orc.cliente, tamanho = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                orc.cliente,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Filled.Star,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                orc.avaliacao.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "Cliente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Verde.copy(alpha = 0.12f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Valor total aprovado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Verde,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        orc.valor,
                        style = MaterialTheme.typography.titleMedium,
                        color = Verde,
                        fontWeight = FontWeight.Bold
                    )
                }

                SecaoLabel("Descrição do serviço", icone = Icons.Outlined.Description)
                Text(
                    orc.descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CaixaInfo(
                        Icons.Outlined.NearMe,
                        "Distância",
                        orc.distancia,
                        Modifier.weight(1f)
                    )
                    CaixaInfo(Icons.Outlined.Schedule, "Horário", orc.horario, Modifier.weight(1f))
                }
                CaixaInfo(
                    Icons.Outlined.LocationOn,
                    "Endereço",
                    orc.endereco,
                    Modifier.fillMaxWidth()
                )
                CaixaInfo(
                    Icons.Outlined.CalendarToday,
                    "Data agendada",
                    orc.dataAgendada,
                    Modifier.fillMaxWidth()
                )

                SecaoLabel("Composição do valor", icone = Icons.Outlined.Paid)
                orc.itens.forEach { item ->
                    LinhaValor(item.descricao, item.valor)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Verde.copy(alpha = 0.10f))
                        .padding(12.dp),
                ) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        orc.valor,
                        style = MaterialTheme.typography.titleMedium,
                        color = Verde,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (orc.observacoes.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Ambar.copy(alpha = 0.10f))
                            .border(1.dp, Ambar.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.StickyNote2,
                                null,
                                tint = Ambar,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "OBSERVAÇÕES",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ambar,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            orc.observacoes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.size(2.dp))
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 16.dp)
                    .navigationBarsPadding(),
            ) {
                Button(
                    onClick = { esconderEntao(scope, sheetState, onFechar) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.fechar))
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Peças reutilizadas                                                 */
/* ------------------------------------------------------------------ */

@Composable
private fun CabecalhoSheet(
    titulo: String,
    subtitulo: String,
    onFechar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onFechar,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                Icons.Outlined.Close,
                "Fechar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SecaoLabel(texto: String, icone: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icone != null) {
            Icon(
                icone,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            texto.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CaixaInfo(
    icone: ImageVector,
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icone,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                rotulo.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.size(4.dp))
        Text(
            valor,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LinhaValor(rotulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            rotulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
