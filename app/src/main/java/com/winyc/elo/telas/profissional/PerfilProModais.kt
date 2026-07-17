package com.winyc.elo.telas.profissional

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.winyc.elo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
private fun fecharSheet(scope: CoroutineScope, sheetState: SheetState, aoFim: () -> Unit) {
    scope.launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) aoFim()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditarPerfilPublicoSheet(
    perfil: PerfilPublico,
    onSalvar: (PerfilPublico) -> Unit,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    var nome by rememberSaveable { mutableStateOf(perfil.nome) }
    var fotoUrl by rememberSaveable { mutableStateOf(perfil.fotoUrl) }
    var bio by rememberSaveable { mutableStateOf(perfil.bio) }
    var area by remember { mutableStateOf(perfil.area) }
    val tags = remember { mutableStateListOf<String>().apply { addAll(perfil.tags) } }

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.92f)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                CabecalhoSheetPro(
                    titulo = stringResource(R.string.pro_editar_perfil_publico),
                    subtitulo = stringResource(R.string.pro_editar_sub),
                    onFechar = { fecharSheet(scope, sheetState, onFechar) },
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.size(2.dp))

                // Foto: preview (iniciais) + URL.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarCliente(nome, tamanho = 56.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.pro_url_foto),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.size(4.dp))
                        OutlinedTextField(
                            value = fotoUrl,
                            onValueChange = { fotoUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.pro_url_foto_hint)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            colors = coresCampo(),
                        )
                    }
                }

                CampoPro(stringResource(R.string.perfil_campo_nome), nome, { nome = it }, stringResource(R.string.perfil_campo_nome_hint))
                CampoPro(
                    stringResource(R.string.pro_bio), bio, { bio = it },
                    stringResource(R.string.pro_bio_hint), linhas = 4,
                )
                SeletorAreaAtendimento(area = area, onArea = { area = it })

                ChipsEditaveis(
                    titulo = stringResource(R.string.pro_tags_especialidade),
                    itens = tags,
                    hint = stringResource(R.string.pro_tags_hint),
                )
                Spacer(Modifier.size(2.dp))
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 16.dp)
                    .navigationBarsPadding(),
            ) {
                Button(
                    onClick = {
                        onSalvar(
                            perfil.copy(
                                nome = nome.trim(),
                                fotoUrl = fotoUrl.trim(),
                                bio = bio.trim(),
                                area = area,
                                tags = tags.toList(),
                            )
                        )
                        fecharSheet(scope, sheetState, onFechar)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nome.isNotBlank(),
                ) {
                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.pro_salvar_perfil))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MeusServicosSheet(
    servicos: SnapshotStateList<ServicoPro>,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    var editando by remember { mutableStateOf<ServicoPro?>(null) }
    var criando by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        dragHandle = null,
    ) {
        Box(modifier = Modifier.fillMaxHeight(0.92f).padding(vertical = 20.dp)) {
            when {
                criando -> FormServico(
                    inicial = null,
                    onVoltar = { criando = false },
                    onSalvar = { novo ->
                        val proxId = (servicos.maxOfOrNull { it.id } ?: 0) + 1
                        servicos.add(novo.copy(id = proxId))
                        criando = false
                    },
                )

                editando != null -> FormServico(
                    inicial = editando,
                    onVoltar = { editando = null },
                    onSalvar = { alterado ->
                        val i = servicos.indexOfFirst { it.id == alterado.id }
                        if (i >= 0) servicos[i] = alterado
                        editando = null
                    },
                )

                else -> ListaServicos(
                    servicos = servicos,
                    onFechar = { fecharSheet(scope, sheetState, onFechar) },
                    onNovo = { criando = true },
                    onEditar = { editando = it },
                    onExcluir = { alvo -> servicos.removeAll { it.id == alvo.id } },
                )
            }
        }
    }
}

@Composable
private fun ListaServicos(
    servicos: List<ServicoPro>,
    onFechar: () -> Unit,
    onNovo: () -> Unit,
    onEditar: (ServicoPro) -> Unit,
    onExcluir: (ServicoPro) -> Unit,
) {
    // Áreas em que o profissional atua; a lista abre filtrada pela primeira.
    val areas = servicos.map { it.categoria }.distinct()
    var areaSelecionada by rememberSaveable { mutableStateOf<String?>(null) }
    val areaAtual = areaSelecionada?.takeIf { it in areas } ?: areas.firstOrNull()
    val visiveis = servicos.filter { it.categoria == areaAtual }

    Column(modifier = Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            CabecalhoSheetPro(
                titulo = stringResource(R.string.pro_meus_servicos),
                subtitulo = stringResource(R.string.pro_servicos_sub),
                onFechar = onFechar,
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

            // Filtro por área de atuação.
            if (areas.isNotEmpty()) {
                FiltroAreas(
                    areas = areas,
                    selecionada = areaAtual,
                    onSelecionar = { areaSelecionada = it },
                )
            }

            Text(
                stringResource(R.string.pro_servicos_qtd, visiveis.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            visiveis.forEach { servico ->
                CardServico(
                    servico = servico,
                    onEditar = { onEditar(servico) },
                    onExcluir = { onExcluir(servico) },
                )
            }
            Spacer(Modifier.size(2.dp))
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 16.dp)
                .navigationBarsPadding(),
        ) {
            Button(onClick = onNovo, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.pro_adicionar_servico))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FiltroAreas(
    areas: List<String>,
    selecionada: String?,
    onSelecionar: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        areas.forEach { area ->
            val sel = area == selecionada
            FilterChip(
                selected = sel,
                onClick = { onSelecionar(area) },
                label = { Text(area, fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = sel,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
private fun CardServico(servico: ServicoPro, onEditar: () -> Unit, onExcluir: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.LocalOffer,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                servico.titulo,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            BotaoIcone(Icons.Outlined.Edit, stringResource(R.string.pro_editar_servico_cd), MaterialTheme.colorScheme.primary, onEditar)
            Spacer(Modifier.width(6.dp))
            BotaoIcone(Icons.Outlined.DeleteOutline, stringResource(R.string.pro_excluir_servico_cd), MaterialTheme.colorScheme.error, onExcluir)
        }

        Text(
            servico.descricao,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            stringResource(R.string.a_partir_de, servico.faixaPreco),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )

        if (servico.pontos.isNotEmpty()) {
            FlowRowChips(servico.pontos)
        }
    }
}

@Composable
private fun BotaoIcone(icone: ImageVector, cd: String, cor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(cor.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, cd, tint = cor, modifier = Modifier.size(16.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowChips(itens: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        itens.forEach { ponto ->
            Text(
                ponto,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

private enum class AreaAtuacao(val labelRes: Int) {
    Limpeza(R.string.pro_cat_limpeza),
    Reformas(R.string.pro_cat_reformas),
    Instalacoes(R.string.pro_cat_instalacoes),
    Moveis(R.string.pro_cat_moveis),
    Externos(R.string.pro_cat_externos),
    Assistencia(R.string.pro_cat_assistencia),
    Digitais(R.string.pro_cat_digitais),
    Profissionais(R.string.pro_cat_profissionais),
}

private enum class DiaSemana(val curtoRes: Int, val longoRes: Int) {
    Dom(R.string.pro_dia_dom, R.string.pro_dia_domingo),
    Seg(R.string.pro_dia_seg, R.string.pro_dia_segunda),
    Ter(R.string.pro_dia_ter, R.string.pro_dia_terca),
    Qua(R.string.pro_dia_qua, R.string.pro_dia_quarta),
    Qui(R.string.pro_dia_qui, R.string.pro_dia_quinta),
    Sex(R.string.pro_dia_sex, R.string.pro_dia_sexta),
    Sab(R.string.pro_dia_sab, R.string.pro_dia_sabado),
}

@Stable
private class IntervaloEstado(inicio: String, fim: String) {
    var inicio by mutableStateOf(inicio)
    var fim by mutableStateOf(fim)
}

@Stable
private class DiaEstado(val dia: DiaSemana) {
    var ativo by mutableStateOf(false)
    val intervalos = mutableStateListOf<IntervaloEstado>()
}

@Composable
private fun FormServico(
    inicial: ServicoPro?,
    onVoltar: () -> Unit,
    onSalvar: (ServicoPro) -> Unit,
) {
    val context = LocalContext.current
    val editando = inicial != null
    val adicionarLabel = stringResource(R.string.pro_adicionar)
    val nomesAreas = AreaAtuacao.entries.associateWith { stringResource(it.labelRes) }
    val areaInicial = inicial?.let { servico ->
        AreaAtuacao.entries.firstOrNull { nomesAreas[it] == servico.categoria }
    }

    var area by rememberSaveable {
        mutableStateOf(areaInicial)
    }
    var titulo by rememberSaveable { mutableStateOf(inicial?.titulo ?: "") }
    var descricao by rememberSaveable { mutableStateOf(inicial?.descricao ?: "") }
    var tempoExpe by rememberSaveable { mutableStateOf(inicial?.tempoExpe?.toString() ?: "") }
    var faixaPreco by rememberSaveable { mutableStateOf(inicial?.faixaPreco ?: "") }
    val pontos = remember { mutableStateListOf<String>().apply { inicial?.let { addAll(it.pontos) } } }
    val dias = remember { DiaSemana.entries.map { DiaEstado(it) } }

    val podeSalvar = area != null && titulo.isNotBlank()
    val categoriaSelecionada = area?.let { stringResource(it.labelRes) }

    Column(modifier = Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            CabecalhoSheetPro(
                titulo = stringResource(if (editando) R.string.pro_editar_servico else R.string.pro_novo_servico),
                subtitulo = stringResource(R.string.pro_servico_sub),
                onFechar = onVoltar,
                iconeFechar = Icons.AutoMirrored.Filled.ArrowBack,
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

            // 1. Área de atuação
            SecaoTitulo(stringResource(R.string.pro_area_atuacao))
            FlowRowAreas(selecionada = area, onSelecionar = { area = it })
            area?.let {
                Text(
                    stringResource(R.string.pro_area_selecionada, stringResource(it.labelRes)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Tempo de experiencia
                CampoPro(stringResource(R.string.tempo_experiencia, stringResource(it.labelRes)), tempoExpe, { tempoExpe = it }, "", tipoCampo = KeyboardType.Number)
            }

            // 2. Serviço específico (só depois de escolher a área)
            if (area != null) {
                SecaoTitulo(stringResource(R.string.pro_servico_especifico))
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.pro_servico_especifico_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = coresCampo(),
                )
                if (titulo.isNotBlank()) {
                    Text(
                        stringResource(R.string.pro_servico_aparece, stringResource(area!!.labelRes), titulo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Descrição
            CampoPro(stringResource(R.string.descricao), descricao, { descricao = it }, stringResource(R.string.pro_descricao_hint), linhas = 3)

            // Imagens
            Text(stringResource(R.string.pro_imagens), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) {
                    SlotImagem(
                        onClick = { Toast.makeText(context, adicionarLabel, Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Text(stringResource(R.string.pro_imagens_dica), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Faixa de preço
            CampoPro(stringResource(R.string.a_partir) + " R$", faixaPreco, { faixaPreco = it }, "300", tipoCampo = KeyboardType.Number)

            // Principais pontos
            ChipsEditaveis(
                titulo = stringResource(R.string.pro_principais_pontos),
                itens = pontos,
                hint = stringResource(R.string.pro_pontos_hint),
            )

            // Disponibilidade semanal
            DisponibilidadeSemanal(dias)
            Spacer(Modifier.size(2.dp))
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Button(
                onClick = {
                    onSalvar(
                        ServicoPro(
                            id = inicial?.id ?: 0,
                            categoria = requireNotNull(categoriaSelecionada),
                            titulo = titulo.trim(),
                            descricao = descricao.trim(),
                            faixaPreco = faixaPreco.trim(),
                            pontos = pontos.toList(),
                            tempoExpe = tempoExpe.toIntOrNull() ?: 0,
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = podeSalvar,
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.pro_salvar_servico))
            }
            if (!podeSalvar) {
                Text(
                    stringResource(R.string.pro_selecione_categoria),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowAreas(selecionada: AreaAtuacao?, onSelecionar: (AreaAtuacao) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AreaAtuacao.entries.forEach { opcao ->
            val sel = opcao == selecionada
            val cor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            Text(
                stringResource(opcao.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = cor,
                fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier
                    .clip(CircleShape)
                    .then(
                        if (sel) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                        else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    .clickable { onSelecionar(opcao) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun SlotImagem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Outlined.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(4.dp))
        Text(stringResource(R.string.pro_adicionar), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun DisponibilidadeSemanal(dias: List<DiaEstado>) {
    val algumAtivo = dias.any { it.ativo }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.pro_disponibilidade).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            if (algumAtivo) {
                Text(
                    stringResource(R.string.pro_igualar_todos),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { igualarTodos(dias) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        // Círculos dos dias.
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            dias.forEach { estado ->
                CirculoDia(estado = estado, modifier = Modifier.weight(1f))
            }
        }

        if (!algumAtivo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.pro_disp_vazio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            dias.filter { it.ativo }.forEach { estado ->
                CardDiaIntervalos(estado)
            }
        }

        Text(
            stringResource(R.string.pro_disp_dica),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CirculoDia(estado: DiaEstado, modifier: Modifier = Modifier) {
    val ativo = estado.ativo
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .then(
                if (ativo) Modifier.background(MaterialTheme.colorScheme.primary)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            .clickable { alternarDia(estado) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(estado.dia.curtoRes),
            style = MaterialTheme.typography.labelMedium,
            color = if (ativo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (ativo) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Composable
private fun CardDiaIntervalos(estado: DiaEstado) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(estado.dia.longoRes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { estado.intervalos.add(IntervaloEstado("08:00", "18:00")) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.pro_horario), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        estado.intervalos.forEachIndexed { i, intervalo ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                CampoHora(intervalo.inicio, { intervalo.inicio = it }, Modifier.weight(1f))
                Text(" – ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CampoHora(intervalo.fim, { intervalo.fim = it }, Modifier.weight(1f))
                IconButton(onClick = {
                    estado.intervalos.removeAt(i)
                    if (estado.intervalos.isEmpty()) estado.ativo = false
                }) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.pro_remover), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun CampoHora(valor: String, onValor: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = coresCampo(),
    )
}

private fun alternarDia(estado: DiaEstado) {
    estado.ativo = !estado.ativo
    if (estado.ativo && estado.intervalos.isEmpty()) {
        estado.intervalos.add(IntervaloEstado("08:00", "18:00"))
    }
}

private fun igualarTodos(dias: List<DiaEstado>) {
    val base = dias.firstOrNull { it.ativo } ?: return
    val modelo = base.intervalos.map { IntervaloEstado(it.inicio, it.fim) }
    dias.forEach { d ->
        d.ativo = true
        d.intervalos.clear()
        modelo.forEach { d.intervalos.add(IntervaloEstado(it.inicio, it.fim)) }
    }
}


@Composable
private fun CabecalhoSheetPro(
    titulo: String,
    subtitulo: String,
    onFechar: () -> Unit,
    iconeFechar: ImageVector = Icons.Outlined.Close,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(
            onClick = onFechar,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(iconeFechar, stringResource(R.string.fechar), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SecaoTitulo(texto: String) {
    Text(
        texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun CampoPro(
    label: String,
    valor: String,
    onValor: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    linhas: Int = 1,
    tipoCampo: KeyboardType = KeyboardType.Text,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = valor,
            onValueChange = onValor,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (linhas > 1) Modifier.height((44 + linhas * 22).dp) else Modifier),
            placeholder = { Text(placeholder) },
            singleLine = linhas == 1,
            minLines = linhas,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = tipoCampo),
            colors = coresCampo(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsEditaveis(titulo: String, itens: SnapshotStateList<String>, hint: String) {
    var texto by rememberSaveable(titulo) { mutableStateOf("") }

    fun adicionar() {
        val novo = texto.trim()
        if (novo.isNotBlank() && itens.none { it.equals(novo, ignoreCase = true) }) {
            itens.add(novo)
        }
        texto = ""
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(titulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(hint) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = coresCampo(),
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = { adicionar() }, enabled = texto.isNotBlank()) {
                Icon(Icons.Outlined.Add, stringResource(R.string.pro_adicionar_item))
            }
        }
        if (itens.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itens.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { itens.remove(item) }
                            .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    ) {
                        Text(item, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Outlined.Close, stringResource(R.string.pro_remover), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

/* ============================ Área de atendimento (Google Maps) ============================ */

@Composable
private fun SeletorAreaAtendimento(
    area: AreaAtendimento,
    onArea: (AreaAtendimento) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val naoEncontrado = stringResource(R.string.pro_area_nao_encontrado)
    val primary = MaterialTheme.colorScheme.primary
    val areaAtual by rememberUpdatedState(area)

    var busca by rememberSaveable { mutableStateOf("") }
    var buscando by remember { mutableStateOf(false) }

    val markerState = rememberMarkerState(position = LatLng(area.latitude, area.longitude))
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(area.latitude, area.longitude), 13f)
    }

    fun aplicarPonto(p: LatLng) {
        onArea(areaAtual.copy(latitude = p.latitude, longitude = p.longitude))
        reverseGeocodificar(context, p.latitude, p.longitude) { addr ->
            scope.launch {
                onArea(
                    areaAtual.copy(
                        latitude = p.latitude,
                        longitude = p.longitude,
                        cidade = addr?.localidadeOuRegiao() ?: areaAtual.cidade,
                        estado = addr?.adminArea ?: areaAtual.estado,
                        bairro = addr?.subLocality,
                    ),
                )
            }
        }
    }

    LaunchedEffect(markerState) {
        var arrastando = false
        snapshotFlow { markerState.isDragging }.collect { dragging ->
            if (dragging) {
                arrastando = true
            } else if (arrastando) {
                arrastando = false
                aplicarPonto(markerState.position)
            }
        }
    }

    fun buscar() {
        if (busca.isBlank()) return
        buscando = true
        geocodificar(context, busca) { addr ->
            scope.launch {
                buscando = false
                if (addr == null) {
                    Toast.makeText(context, naoEncontrado, Toast.LENGTH_SHORT).show()
                } else {
                    val p = LatLng(addr.latitude, addr.longitude)
                    markerState.position = p
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(p, 13f)
                    onArea(
                        areaAtual.copy(
                            latitude = addr.latitude,
                            longitude = addr.longitude,
                            cidade = addr.localidadeOuRegiao(),
                            estado = addr.adminArea ?: areaAtual.estado,
                            bairro = addr.subLocality,
                        ),
                    )
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(R.string.pro_area_atendimento),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.pro_area_buscar_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { buscar() }),
                colors = coresCampo(),
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = { buscar() }, enabled = busca.isNotBlank() && !buscando) {
                Icon(Icons.Outlined.Search, stringResource(R.string.pro_area_buscar_cd))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    markerState.position = latLng
                    aplicarPonto(latLng)
                },
            ) {
                Marker(state = markerState, draggable = true)
                Circle(
                    center = markerState.position,
                    radius = area.raioKm * 1000.0,
                    strokeColor = primary,
                    strokeWidth = 4f,
                    fillColor = primary.copy(alpha = 0.15f),
                )
            }
            if (buscando) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primary,
                )
            }
        }

        Text(
            stringResource(R.string.pro_area_mapa_dica),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.LocationOn,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(area.local, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    formatarCoord(area.latitude, area.longitude),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.pro_area_raio),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.pro_area_ate_km, area.raioKm),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = area.raioKm.toFloat(),
            onValueChange = { onArea(area.copy(raioKm = it.roundToInt().coerceIn(1, 30))) },
            valueRange = 1f..30f,
            steps = 28,
        )
    }
}


private fun Address.localidadeOuRegiao(): String =
    locality ?: subAdminArea ?: adminArea ?: ""

private fun geocodificar(context: Context, consulta: String, onResultado: (Address?) -> Unit) {
    val termo = consulta.trim()
    if (termo.isEmpty() || !Geocoder.isPresent()) {
        onResultado(null)
        return
    }
    Geocoder(context, Locale.forLanguageTag("pt-BR")).getFromLocationName(
        termo,
        1,
        object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) = onResultado(addresses.firstOrNull())
            override fun onError(errorMessage: String?) = onResultado(null)
        },
    )
}

private fun reverseGeocodificar(context: Context, lat: Double, lng: Double, onResultado: (Address?) -> Unit) {
    if (!Geocoder.isPresent()) {
        onResultado(null)
        return
    }
    Geocoder(context, Locale.forLanguageTag("pt-BR")).getFromLocation(
        lat,
        lng,
        1,
        object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) = onResultado(addresses.firstOrNull())
            override fun onError(errorMessage: String?) = onResultado(null)
        },
    )
}

@Composable
private fun coresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
)
