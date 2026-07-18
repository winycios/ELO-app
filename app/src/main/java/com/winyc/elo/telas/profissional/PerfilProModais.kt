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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.winyc.elo.R
import com.winyc.elo.backend.model.categoria.CategoriaRS
import com.winyc.elo.backend.model.profissional.AreaAtendimentoUpdateDTO
import com.winyc.elo.backend.model.profissional.ProfissionalUpdateDTO
import com.winyc.elo.backend.model.servico.ServicoCreateDTO
import com.winyc.elo.backend.model.servico.ServicoDisponibilidadeCreateDTO
import com.winyc.elo.backend.model.servico.ServicoListaRS
import com.winyc.elo.backend.model.servico.ServicoRS
import com.winyc.elo.backend.viewModel.ProfissionalUi
import com.winyc.elo.backend.viewModel.ProfissionalViewModel
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

private fun PerfilPublico.paraUpdateDTO() = ProfissionalUpdateDTO(
    apresentacao = bio.trim().take(200),
    uriPerfil = fotoUrl.trim().take(200),
    especialidades = tags.joinToString("; ").take(200),
    areaAtendimentoUpdateDTO = AreaAtendimentoUpdateDTO(
        nrLatitude = area.latitude,
        nrLongitude = area.longitude,
        nrRaio = area.raioKm,
        nmCidade = area.cidade,
        nmEstado = area.estado,
        nmBairro = area.bairro?.takeIf { it.isNotBlank() } ?: area.cidade,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditarPerfilPublicoSheet(
    perfil: PerfilPublico,
    vm: ProfissionalViewModel,
    carregando: Boolean,
    onSalvar: (PerfilPublico) -> Unit,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        dragHandle = null,
    ) {
        Box(modifier = Modifier.fillMaxHeight(0.92f)) {
            if (carregando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            } else {
                // Só compõe o formulário depois que os dados chegam, para os campos
                // já iniciarem preenchidos com o perfil carregado.
                FormularioEditarPerfil(
                    perfil = perfil,
                    vm = vm,
                    scope = scope,
                    sheetState = sheetState,
                    onSalvar = onSalvar,
                    onFechar = onFechar,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioEditarPerfil(
    perfil: PerfilPublico,
    vm: ProfissionalViewModel,
    scope: CoroutineScope,
    sheetState: SheetState,
    onSalvar: (PerfilPublico) -> Unit,
    onFechar: () -> Unit,
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    var nome by rememberSaveable { mutableStateOf(perfil.nome) }
    var fotoUrl by rememberSaveable { mutableStateOf(perfil.fotoUrl) }
    var bio by rememberSaveable { mutableStateOf(perfil.bio) }
    var area by remember { mutableStateOf(perfil.area) }
    val tags = remember { mutableStateListOf<String>().apply { addAll(perfil.tags) } }
    var mapaEmUso by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxHeight()) {
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
                    .verticalScroll(rememberScrollState(), enabled = !mapaEmUso)
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

                CampoPro(
                    label = stringResource(R.string.perfil_campo_nome),
                    valor = nome,
                    onValor = { nome = it },
                    placeholder = stringResource(R.string.perfil_campo_nome_hint),
                    habilitado = false,
                    apoio = stringResource(R.string.pro_nome_apoio),
                )
                CampoPro(
                    stringResource(R.string.pro_bio), bio, { bio = it },
                    stringResource(R.string.pro_bio_hint), linhas = 4,
                )
                SeletorAreaAtendimento(
                    area = area,
                    onArea = { area = it },
                    onMapaEmUso = { mapaEmUso = it },
                )

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
                        val atualizado = perfil.copy(
                            nome = nome.trim(),
                            fotoUrl = fotoUrl.trim(),
                            bio = bio.trim(),
                            area = area,
                            tags = tags.toList(),
                        )
                        vm.salvarPerfil(atualizado.paraUpdateDTO()) { ok ->
                            if (ok) {
                                onSalvar(atualizado)
                                fecharSheet(scope, sheetState, onFechar)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nome.isNotBlank() && !estado.salvandoPerfil,
                ) {
                    if (estado.salvandoPerfil) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
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
    vm: ProfissionalViewModel,
    categorias: List<CategoriaRS>,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val estado by vm.estado.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<ServicoRS?>(null) }
    var criando by remember { mutableStateOf(false) }
    var carregandoEdicao by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        dragHandle = null,
    ) {
        Box(modifier = Modifier.fillMaxHeight(0.92f).padding(vertical = 20.dp)) {
            when {
                criando -> FormServico(
                    detalhe = null,
                    categorias = categorias,
                    salvando = estado.salvando,
                    onVoltar = { criando = false },
                    onSalvar = { dto -> vm.salvarServico(dto) { ok -> if (ok) criando = false } },
                )

                editando != null -> FormServico(
                    detalhe = editando,
                    categorias = categorias,
                    salvando = estado.salvando,
                    onVoltar = { editando = null },
                    onSalvar = { dto -> vm.salvarServico(dto) { ok -> if (ok) editando = null } },
                )

                else -> ListaServicos(
                    estado = estado,
                    categorias = categorias,
                    carregandoEdicao = carregandoEdicao,
                    onFechar = { fecharSheet(scope, sheetState, onFechar) },
                    onNovo = { criando = true },
                    onEditar = { id ->
                        carregandoEdicao = id
                        vm.buscarServico(id) { rs ->
                            carregandoEdicao = null
                            if (rs != null) editando = rs
                        }
                    },
                    onExcluir = { vm.excluirServico(it) },
                )
            }
        }
    }
}

@Composable
private fun ListaServicos(
    estado: ProfissionalUi,
    categorias: List<CategoriaRS>,
    carregandoEdicao: Long?,
    onFechar: () -> Unit,
    onNovo: () -> Unit,
    onEditar: (Long) -> Unit,
    onExcluir: (Long) -> Unit,
) {
    val nomeEspecifica = remember(categorias) {
        categorias.flatMap { it.categoriaEspecificaList }.associate { it.id to it.nmCategoria }
    }
    val nomeGeral = remember(categorias) {
        categorias.flatMap { it.categoriaEspecificaList }
            .associate { it.categoriaGeral.id to it.categoriaGeral.nmCategoria }
    }

    val servicos = estado.servicos
    // Áreas (categoria geral) em que o profissional atua; abre filtrada pela primeira.
    val gerais = servicos.mapNotNull { it.categoria?.idCategoriaGeral }.distinct()
    var geralSelecionada by rememberSaveable { mutableStateOf<Long?>(null) }
    val geralAtual = geralSelecionada?.takeIf { it in gerais } ?: gerais.firstOrNull()
    val visiveis = servicos.filter { it.categoria?.idCategoriaGeral == geralAtual }

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

            when {
                estado.carregando && servicos.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(modifier = Modifier.size(28.dp)) }

                servicos.isEmpty() -> Text(
                    stringResource(R.string.pro_servicos_vazio),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )

                else -> {
                    if (gerais.size > 1) {
                        FiltroAreas(
                            areas = gerais.map { it to (nomeGeral[it] ?: "—") },
                            selecionada = geralAtual,
                            onSelecionar = { geralSelecionada = it },
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
                            nome = nomeEspecifica[servico.categoria?.idCategoriaEspecifica]
                                ?: stringResource(R.string.pro_servico_sem_nome),
                            carregando = carregandoEdicao == servico.id,
                            onEditar = { onEditar(servico.id) },
                            onExcluir = { onExcluir(servico.id) },
                        )
                    }
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
    areas: List<Pair<Long, String>>,
    selecionada: Long?,
    onSelecionar: (Long) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        areas.forEach { (id, nome) ->
            val sel = id == selecionada
            FilterChip(
                selected = sel,
                onClick = { onSelecionar(id) },
                label = { Text(nome, fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal) },
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
private fun CardServico(
    servico: ServicoListaRS,
    nome: String,
    carregando: Boolean,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
) {
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
                nome,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (carregando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                BotaoIcone(Icons.Outlined.Edit, stringResource(R.string.pro_editar_servico_cd), MaterialTheme.colorScheme.primary, onEditar)
                Spacer(Modifier.width(6.dp))
                BotaoIcone(Icons.Outlined.DeleteOutline, stringResource(R.string.pro_excluir_servico_cd), MaterialTheme.colorScheme.error, onExcluir)
            }
        }

        servico.dsDescricao?.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        servico.vlServico?.let { preco ->
            Text(
                stringResource(R.string.a_partir_de, formatarPreco(preco)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }

        val pontos = servico.dsTag?.split(';', ',')?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
        if (pontos.isNotEmpty()) {
            FlowRowChips(pontos)
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

private data class GeralOpcao(val id: Long, val nome: String)
private data class EspecificaOpcao(val id: Long, val nome: String, val idGeral: Long)

private enum class TipoExecucao(val labelRes: Int, val valorApi: String) {
    Presencial(R.string.pro_exec_presencial, "presencial"),
    Remoto(R.string.pro_exec_remoto, "remoto");

    companion object {
        fun fromApi(valor: String?): TipoExecucao? = entries.firstOrNull {
            it.valorApi.equals(valor, ignoreCase = true) || it.name.equals(valor, ignoreCase = true)
        }
    }
}

private fun formatarPreco(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toLong().toString()
    else String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valor)

private fun horaDigitos(hora: String?): String = hora.orEmpty().filter { it.isDigit() }.take(4)

/** Dígitos "0800" → "HH:mm:ss" esperado pelo LocalTime do backend. */
private fun normalizarHora(digitos: String): String {
    val d = digitos.filter { it.isDigit() }.take(4).padEnd(4, '0')
    val hh = (d.substring(0, 2).toIntOrNull() ?: 0).coerceIn(0, 23)
    val mm = (d.substring(2, 4).toIntOrNull() ?: 0).coerceIn(0, 59)
    return "%02d:%02d:00".format(hh, mm)
}

/** Aceita dígitos que ainda podem formar um horário válido (permite vazio ao editar). */
private fun horaParcialValida(digitos: String): Boolean {
    if (digitos.isEmpty()) return true
    if (digitos[0] !in '0'..'2') return false
    if (digitos.length >= 2 && digitos.substring(0, 2).toInt() > 23) return false
    if (digitos.length >= 3 && digitos[2] !in '0'..'5') return false
    return true
}

/** Horário completo = 4 dígitos preenchidos. */
private fun horaCompleta(digitos: String): Boolean = digitos.length == 4

/** "0830" → 510 (minutos desde 00:00), para comparar início x fim. */
private fun minutosDoDia(digitos: String): Int {
    val d = digitos.padEnd(4, '0')
    return (d.substring(0, 2).toIntOrNull() ?: 0) * 60 + (d.substring(2, 4).toIntOrNull() ?: 0)
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
    detalhe: ServicoRS?,
    categorias: List<CategoriaRS>,
    salvando: Boolean,
    onVoltar: () -> Unit,
    onSalvar: (ServicoCreateDTO) -> Unit,
) {
    val context = LocalContext.current
    val editando = detalhe != null
    val adicionarLabel = stringResource(R.string.pro_adicionar)

    val especificas = remember(categorias) {
        categorias.flatMap { it.categoriaEspecificaList }
            .map { EspecificaOpcao(it.id, it.nmCategoria, it.categoriaGeral.id) }
    }
    val gerais = remember(categorias) {
        categorias.flatMap { it.categoriaEspecificaList }
            .map { it.categoriaGeral }
            .distinctBy { it.id }
            .map { GeralOpcao(it.id, it.nmCategoria) }
    }

    var idGeral by rememberSaveable { mutableStateOf(detalhe?.servicoCategoriaRS?.idCategoriaGeral) }
    var idEspecifica by rememberSaveable { mutableStateOf(detalhe?.servicoCategoriaRS?.idCategoriaEspecifica) }
    var tempoExpe by rememberSaveable { mutableStateOf(detalhe?.tempoExperiencia?.toString() ?: "") }
    var descricao by rememberSaveable { mutableStateOf(detalhe?.dsDescricao ?: "") }
    var faixaPreco by rememberSaveable { mutableStateOf(detalhe?.vlServico?.let { formatarPreco(it) } ?: "") }
    var tipoExec by rememberSaveable { mutableStateOf(TipoExecucao.fromApi(detalhe?.tpExecucao)) }
    val pontos = remember {
        mutableStateListOf<String>().apply {
            detalhe?.dsTag?.split(';', ',')?.map { it.trim() }?.filter { it.isNotBlank() }?.let { addAll(it) }
        }
    }
    val dias = remember(detalhe) {
        DiaSemana.entries.map { DiaEstado(it) }.also { lista ->
            detalhe?.servicoDisponibilidadeRSList?.forEach { disp ->
                val estado = disp.diaSemana?.let { lista.getOrNull(it) } ?: return@forEach
                estado.ativo = true
                estado.intervalos.add(IntervaloEstado(horaDigitos(disp.hrInicio), horaDigitos(disp.hrFim)))
            }
        }
    }

    val especificasDaArea = especificas.filter { it.idGeral == idGeral }
    val nomeGeral = gerais.firstOrNull { it.id == idGeral }?.nome
    val nomeEspecifica = especificas.firstOrNull { it.id == idEspecifica }?.nome
    val precoValido = (faixaPreco.replace(',', '.').toDoubleOrNull() ?: -1.0) >= 0.0

    val diasAtivos = dias.filter { it.ativo }
    val disponibilidadeValida = diasAtivos.isNotEmpty() && diasAtivos.all { dia ->
        dia.intervalos.isNotEmpty() && dia.intervalos.all { intervalo ->
            horaCompleta(intervalo.inicio) && horaCompleta(intervalo.fim) &&
                minutosDoDia(intervalo.inicio) < minutosDoDia(intervalo.fim)
        }
    }

    val podeSalvar = idGeral != null && idEspecifica != null && descricao.isNotBlank() &&
        precoValido && (tempoExpe.toIntOrNull() ?: -1) >= 0 && tipoExec != null &&
        disponibilidadeValida && !salvando

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

            SecaoTitulo(stringResource(R.string.pro_area_atuacao))
            if (gerais.isEmpty()) {
                Text(
                    stringResource(R.string.pro_categorias_carregando),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ChipsSelecao(
                    opcoes = gerais.map { it.id to it.nome },
                    selecionado = idGeral,
                    onSelecionar = { novo ->
                        if (novo != idGeral) {
                            idGeral = novo
                            idEspecifica = null
                        }
                    },
                )
            }
            nomeGeral?.let { geral ->
                Text(
                    stringResource(R.string.pro_area_selecionada, geral),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                // Tempo de experiência
                CampoPro(stringResource(R.string.tempo_experiencia, geral), tempoExpe, { tempoExpe = it }, "", tipoCampo = KeyboardType.Number)
            }

            // 2. Serviço específico (pré-definidos filtrados pela área)
            if (idGeral != null) {
                SecaoTitulo(stringResource(R.string.pro_servico_especifico))
                if (especificasDaArea.isEmpty()) {
                    Text(
                        stringResource(R.string.pro_servico_sem_especificos),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    ChipsSelecao(
                        opcoes = especificasDaArea.map { it.id to it.nome },
                        selecionado = idEspecifica,
                        onSelecionar = { idEspecifica = it },
                    )
                }
                if (nomeGeral != null && nomeEspecifica != null) {
                    Text(
                        stringResource(R.string.pro_servico_aparece, nomeGeral, nomeEspecifica),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Descrição
            CampoPro(stringResource(R.string.descricao), descricao, { descricao = it }, stringResource(R.string.pro_descricao_hint), linhas = 3)

            // Tipo de execução
            SecaoTitulo(stringResource(R.string.pro_tipo_execucao))
            ChipsSelecaoTexto(
                opcoes = TipoExecucao.entries.map { it to stringResource(it.labelRes) },
                selecionado = tipoExec,
                onSelecionar = { tipoExec = it },
            )

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
                    val idEsp = idEspecifica ?: return@Button
                    val exec = tipoExec ?: return@Button
                    onSalvar(
                        ServicoCreateDTO(
                            id = detalhe?.id,
                            idCategoriaEspecifica = idEsp,
                            dsDescricao = descricao.trim(),
                            vlServico = faixaPreco.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            dsTag = pontos.joinToString("; ").ifBlank { nomeEspecifica ?: "Serviço" },
                            tempoExperiencia = tempoExpe.toIntOrNull() ?: 0,
                            tpExecucao = exec.valorApi,
                            servicoDisponibilidadeCreateDTOList = dias.filter { it.ativo }.flatMap { d ->
                                d.intervalos.map {
                                    ServicoDisponibilidadeCreateDTO(
                                        diaSemana = d.dia.ordinal,
                                        hrInicio = normalizarHora(it.inicio),
                                        hrFim = normalizarHora(it.fim),
                                    )
                                }
                            },
                            servicoImagemCreateDTOList = emptyList(),
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = podeSalvar,
            ) {
                if (salvando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.pro_salvar_servico))
                }
            }
            if (!podeSalvar && !salvando) {
                val aviso = if (idGeral != null && idEspecifica != null && !disponibilidadeValida) {
                    stringResource(R.string.pro_disp_invalida)
                } else {
                    stringResource(R.string.pro_selecione_categoria)
                }
                Text(
                    aviso,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsSelecao(
    opcoes: List<Pair<Long, String>>,
    selecionado: Long?,
    onSelecionar: (Long) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        opcoes.forEach { (id, nome) ->
            ChipEscolha(texto = nome, selecionado = id == selecionado) { onSelecionar(id) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipsSelecaoTexto(
    opcoes: List<Pair<T, String>>,
    selecionado: T?,
    onSelecionar: (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        opcoes.forEach { (valor, nome) ->
            ChipEscolha(texto = nome, selecionado = valor == selecionado) { onSelecionar(valor) }
        }
    }
}

@Composable
private fun ChipEscolha(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    val cor = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        color = cor,
        fontWeight = if (selecionado) FontWeight.Medium else FontWeight.Normal,
        modifier = Modifier
            .clip(CircleShape)
            .then(
                if (selecionado) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
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
                    .clickable { estado.intervalos.add(IntervaloEstado("0800", "1800")) }
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
        onValueChange = { novo ->
            val d = novo.filter { it.isDigit() }.take(4)
            if (horaParcialValida(d)) onValor(d)
        },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = MascaraHora,
        colors = coresCampo(),
    )
}

/** Máscara HH:mm exibida sobre os 4 dígitos digitados (ex.: "0830" → "08:30"). */
private val MascaraHora = VisualTransformation { text ->
    val digitos = text.text.filter { it.isDigit() }.take(4)
    val formatado = if (digitos.length <= 2) digitos else digitos.substring(0, 2) + ":" + digitos.substring(2)
    val mapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = if (offset <= 2) offset else offset + 1
        override fun transformedToOriginal(offset: Int): Int = if (offset <= 2) offset else offset - 1
    }
    TransformedText(AnnotatedString(formatado), mapping)
}

private fun alternarDia(estado: DiaEstado) {
    estado.ativo = !estado.ativo
    if (estado.ativo && estado.intervalos.isEmpty()) {
        estado.intervalos.add(IntervaloEstado("0800", "1800"))
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
    habilitado: Boolean = true,
    apoio: String? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = valor,
            onValueChange = onValor,
            enabled = habilitado,
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
        apoio?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
    onMapaEmUso: (Boolean) -> Unit = {},
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
                .clip(RoundedCornerShape(16.dp))
                // Observa os toques antes do mapa (fase Initial) e avisa o container para
                // não roubar o gesto de arrastar; assim o mapa navega em vez da sheet rolar.
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val evento = awaitPointerEvent(PointerEventPass.Initial)
                            onMapaEmUso(evento.changes.any { it.pressed })
                        }
                    }
                },
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
