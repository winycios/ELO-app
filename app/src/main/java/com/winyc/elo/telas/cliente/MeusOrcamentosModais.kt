package com.winyc.elo.telas.cliente

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.winyc.elo.R
import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheRS
import com.winyc.elo.backend.viewModel.OrcamentoDetalheUi
import com.winyc.elo.backend.viewModel.VisaoOrcamento
import com.winyc.elo.telas.componentes.AvatarPerfil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Locale

private val Verde = Color(0xFF12A15A)

/** Anima o fechamento do sheet e só então limpa o estado no chamador. */
@OptIn(ExperimentalMaterial3Api::class)
fun recolherBottomModal(scope: CoroutineScope, sheetState: SheetState, aoFim: () -> Unit) {
    scope.launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) aoFim()
    }
}

/* ------------------------------------------------------------------ */
/* Folha única do orçamento: detalhes, orçamento final e contato      */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetalheOrcamentoSheet(
    estado: OrcamentoDetalheUi,
    onFechar: () -> Unit,
    onTentarNovamente: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val fechar = { recolherBottomModal(scope, sheetState, onFechar) }
    val detalhe = estado.detalhe

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            when {
                detalhe == null || estado.carregando -> {
                    CabecalhoSheet(titulo = tituloDaVisao(estado.visao), onFechar = fechar)
                    if (estado.erro != null && !estado.carregando) {
                        ConteudoErro(mensagem = estado.erro, onTentarNovamente = onTentarNovamente)
                    } else {
                        CaixaCarregando()
                    }
                }

                estado.visao == VisaoOrcamento.Contato -> ConteudoContato(
                    nome = detalhe.profissional?.nome.orEmpty(),
                    fotoUrl = detalhe.profissional?.fotoPerfil,
                    subtitulo = detalhe.profissional?.categoria,
                    telefone = detalhe.profissional?.contato?.telefone,
                    whatsapp = detalhe.profissional?.contato?.whatsapp,
                    verificado = detalhe.profissional?.verificado == true,
                    onFechar = fechar,
                )

                estado.visao == VisaoOrcamento.OrcamentoFinal ->
                    ConteudoOrcamentoFinal(detalhe = detalhe, onFechar = fechar)

                else -> ConteudoDetalhes(detalhe = detalhe, onFechar = fechar)
            }
        }
    }
}

@Composable
private fun tituloDaVisao(visao: VisaoOrcamento): String = stringResource(
    when (visao) {
        VisaoOrcamento.Contato -> R.string.contato
        VisaoOrcamento.OrcamentoFinal -> R.string.orcamento_final
        VisaoOrcamento.Detalhes -> R.string.detalhes_do_orcamento
    },
)

/* ------------------------------------------------------------------ */
/* Conteúdo: detalhes da solicitação                                  */
/* ------------------------------------------------------------------ */

@Composable
private fun ConteudoDetalhes(detalhe: OrcamentoDetalheRS, onFechar: () -> Unit) {
    val solicitacao = detalhe.solicitacao
    val naoInformado = stringResource(R.string.nao_informado)

    CabecalhoSheet(
        titulo = stringResource(R.string.detalhes_do_orcamento),
        onFechar = onFechar,
    )

    Spacer(Modifier.size(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        AvatarPerfil(
            nome = detalhe.profissional?.nome.orEmpty(),
            fotoUrl = detalhe.profissional?.fotoPerfil,
            tamanho = 44.dp,
            fonte = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = detalhe.profissional?.nome.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        StatusBadge(StatusOrcamento.de(detalhe.status), detalhe.status)
    }

    Spacer(Modifier.size(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.size(16.dp))

    SecaoLabel(stringResource(R.string.informacoes_da_solicitacao))
    Spacer(Modifier.size(12.dp))
    InfoLinha(
        Icons.Outlined.Description,
        stringResource(R.string.descricao),
        solicitacao?.descricao?.takeIf { it.isNotBlank() } ?: naoInformado,
    )
    InfoLinha(
        Icons.Outlined.LocalOffer,
        stringResource(R.string.categoria),
        solicitacao?.categoria?.takeIf { it.isNotBlank() } ?: naoInformado,
    )
    InfoLinha(
        Icons.Outlined.CalendarToday,
        stringResource(R.string.horario_preferido),
        formatarDataHora(solicitacao?.horarioPreferido) ?: naoInformado,
    )
    InfoLinha(
        Icons.Outlined.Handyman,
        stringResource(R.string.tipo_servico),
        rotuloTipoServico(solicitacao?.tipoServico) ?: naoInformado,
    )
    InfoLinha(
        Icons.Outlined.Paid,
        stringResource(R.string.valor_estimado),
        formatarBRL(solicitacao?.valor) ?: stringResource(R.string.a_combinar),
    )

    enderecoCompleto(solicitacao?.endereco)?.let { endereco ->
        Spacer(Modifier.size(10.dp))
        CaixaInfo(
            icone = Icons.Outlined.LocationOn,
            rotulo = stringResource(R.string.endereco),
            valor = endereco,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    val imagens = solicitacao?.imagens.orEmpty()
    if (imagens.isNotEmpty()) {
        Spacer(Modifier.size(16.dp))
        SecaoLabel(stringResource(R.string.imagens_anexadas))
        Spacer(Modifier.size(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(imagens) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }
    }

    Spacer(Modifier.size(20.dp))
    Button(onClick = onFechar, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.fechar))
    }
}

/* ------------------------------------------------------------------ */
/* Conteúdo: orçamento final                                          */
/* ------------------------------------------------------------------ */

@Composable
private fun ConteudoOrcamentoFinal(detalhe: OrcamentoDetalheRS, onFechar: () -> Unit) {
    val final = detalhe.orcamentoFinal
    val solicitacao = detalhe.solicitacao

    CabecalhoSheet(
        icone = Icons.Outlined.EditNote,
        titulo = stringResource(R.string.orcamento_final),
        subtitulo = stringResource(R.string.enviado_pelo_profissional),
        onFechar = onFechar,
    )

    if (final == null) {
        Spacer(Modifier.size(20.dp))
        Text(
            text = stringResource(R.string.orcamento_final_pendente),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(20.dp))
        Button(onClick = onFechar, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.fechar))
        }
        return
    }

    Spacer(Modifier.size(16.dp))
    CabecalhoProfissional(detalhe)

    Spacer(Modifier.size(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.size(16.dp))

    SecaoLabel(stringResource(R.string.servico))
    Spacer(Modifier.size(6.dp))
    Text(
        text = solicitacao?.descricao.orEmpty(),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(Modifier.size(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CaixaInfo(
            icone = Icons.Outlined.CalendarToday,
            rotulo = stringResource(R.string.data),
            valor = formatarData(final.inicioProposto) ?: stringResource(R.string.a_combinar),
            modifier = Modifier.weight(1f),
        )
        CaixaInfo(
            icone = Icons.Outlined.Schedule,
            rotulo = stringResource(R.string.horario),
            valor = faixaDeHorario(final.inicioProposto, final.fimProposto)
                ?: stringResource(R.string.a_combinar),
            modifier = Modifier.weight(1f),
        )
    }

    enderecoCompleto(solicitacao?.endereco)?.let { endereco ->
        Spacer(Modifier.size(10.dp))
        CaixaInfo(
            icone = Icons.Outlined.LocationOn,
            rotulo = null,
            valor = endereco,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(Modifier.size(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.size(16.dp))

    SecaoLabel(stringResource(R.string.valor))
    Spacer(Modifier.size(10.dp))
    final.custos.forEach { custo ->
        LinhaValor(
            rotulo = custo.descricao.orEmpty(),
            valor = formatarBRL(custo.valor) ?: stringResource(R.string.a_combinar),
        )
    }
    Spacer(Modifier.size(8.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.size(8.dp))
    LinhaValor(
        rotulo = stringResource(R.string.total),
        valor = formatarBRL(final.valorTotal) ?: stringResource(R.string.a_combinar),
        destaque = true,
    )

    final.observacaoProfissional?.takeIf { it.isNotBlank() }?.let { observacao ->
        Spacer(Modifier.size(16.dp))
        SecaoLabel(stringResource(R.string.observacao_profissional))
        Spacer(Modifier.size(6.dp))
        Text(
            text = observacao,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
        )
    }

    Spacer(Modifier.size(20.dp))
    Button(onClick = onFechar, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.fechar))
    }
}

/** Bloco com foto, nome, selo de verificado, categoria e avaliação do profissional. */
@Composable
private fun CabecalhoProfissional(detalhe: OrcamentoDetalheRS) {
    val profissional = detalhe.profissional
    Row(verticalAlignment = Alignment.CenterVertically) {
        AvatarPerfil(
            nome = profissional?.nome.orEmpty(),
            fotoUrl = profissional?.fotoPerfil,
            tamanho = 52.dp,
            fonte = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profissional?.nome.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (profissional?.verificado == true) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = stringResource(R.string.perfil_verificado),
                        tint = Verde,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                text = profissional?.categoria.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val avaliacao = profissional?.avaliacao
            if (avaliacao != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${formatarNota(avaliacao)} · ${profissional.quantidadeAvaliacoes ?: 0} " +
                            stringResource(R.string.avaliacoes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Conteúdo: contato                                                  */
/* ------------------------------------------------------------------ */

@Composable
private fun ConteudoContato(
    nome: String,
    fotoUrl: String?,
    subtitulo: String?,
    telefone: String?,
    whatsapp: String?,
    verificado: Boolean,
    onFechar: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val escopo = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CabecalhoSheet(
            icone = Icons.Outlined.Phone,
            titulo = stringResource(R.string.contato),
            onFechar = onFechar,
        )

        Spacer(Modifier.size(16.dp))
        AvatarComStatusOnline(nome = nome, fotoUrl = fotoUrl)

        Spacer(Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = nome,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (verificado) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = stringResource(R.string.perfil_verificado),
                    tint = Verde,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        subtitulo?.takeIf { it.isNotBlank() }?.let { CategoriaChip(it) }

        if (telefone.isNullOrBlank()) {
            Spacer(Modifier.size(20.dp))
            Text(
                text = stringResource(R.string.contato_indisponivel),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(20.dp))
            Button(onClick = onFechar, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.fechar))
            }
            return@Column
        }

        Spacer(Modifier.size(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.numero_de_telefone),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatarTelefone(telefone),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = {
                    escopo.launch {
                        clipboard.setClipEntry(
                            ClipData.newPlainText("Telefone", formatarTelefone(telefone)).toClipEntry(),
                        )
                    }
                    Toast.makeText(context, "Número copiado", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copiar número",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.size(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { context.abrir(Intent(Intent.ACTION_DIAL, "tel:${somenteDigitos(telefone)}".toUri())) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Outlined.Phone, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.ligar))
            }
            Button(
                onClick = {
                    val numero = somenteDigitos(whatsapp?.takeIf { it.isNotBlank() } ?: telefone)
                    context.abrir(Intent(Intent.ACTION_VIEW, "https://wa.me/55$numero".toUri()))
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.whatsapp))
            }
        }

        Spacer(Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = null,
                tint = Verde,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.numero_verificado_plataforma),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/* Modal: Contato (usado pela tela de orçamentos do profissional)     */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContatoSheet(
    nome: String,
    subtitulo: String,
    telefone: String,
    onFechar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        modifier = Modifier.padding(bottom = 120.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            ConteudoContato(
                nome = nome,
                fotoUrl = null,
                subtitulo = subtitulo,
                telefone = telefone,
                whatsapp = telefone,
                verificado = true,
                onFechar = { recolherBottomModal(scope, sheetState, onFechar) },
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/* Modal: Avaliar                                                     */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AvaliarSheet(
    nome: String,
    onFechar: () -> Unit,
    titulo: String = "Avaliar profissional",
    onPublicar: (nota: Int, comentario: String) -> Unit = { _, _ -> },
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var nota by rememberSaveable { mutableIntStateOf(5) }
    var comentario by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onFechar, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.experiencia_com, nome),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                (1..5).forEach { indice ->
                    val ativa = indice <= nota
                    Icon(
                        imageVector = if (ativa) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "$indice estrela(s)",
                        tint = if (ativa) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { nota = indice },
                    )
                }
            }

            Spacer(Modifier.size(16.dp))
            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = { Text(stringResource(R.string.avaliacao_placeholder)) },
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.size(20.dp))
            Button(
                onClick = {
                    onPublicar(nota, comentario.trim())
                    Toast.makeText(context, "Avaliação publicada!", Toast.LENGTH_SHORT).show()
                    recolherBottomModal(scope, sheetState, onFechar)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.publicar_avaliacao))
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Peças reutilizadas pelos modais                                    */
/* ------------------------------------------------------------------ */

/** Cabeçalho padrão: título (com ícone/subtítulo opcionais) + botão de fechar. */
@Composable
private fun CabecalhoSheet(
    titulo: String,
    onFechar: () -> Unit,
    icone: ImageVector? = null,
    subtitulo: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icone != null) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(
            onClick = onFechar,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Fechar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun CaixaCarregando() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
    }
}

@Composable
private fun ConteudoErro(mensagem: String, onTentarNovamente: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onTentarNovamente) {
            Text(stringResource(R.string.vitrine_tentar_novamente))
        }
    }
}

@Composable
private fun AvatarComStatusOnline(nome: String, fotoUrl: String?) {
    Box {
        AvatarPerfil(
            nome = nome,
            fotoUrl = fotoUrl,
            tamanho = 72.dp,
            fonte = MaterialTheme.typography.headlineSmall,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(2.dp)
                .clip(CircleShape)
                .background(Verde),
        )
    }
}

@Composable
private fun CategoriaChip(categoria: String) {
    Text(
        text = categoria,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun SecaoLabel(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )
}

/** Linha "ícone + rótulo + valor" usada nos detalhes do orçamento. */
@Composable
private fun InfoLinha(icone: ImageVector, rotulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Caixa destacada (fundo do contexto) para data/horário/endereço. */
@Composable
private fun CaixaInfo(
    icone: ImageVector,
    rotulo: String?,
    valor: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp),
            )
            if (rotulo != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = rotulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.size(4.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Linha de valor (rótulo à esquerda, valor à direita); total fica em destaque. */
@Composable
private fun LinhaValor(rotulo: String, valor: String, destaque: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = rotulo,
            style = if (destaque) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (destaque) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = valor,
            style = if (destaque) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (destaque) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (destaque) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/** Abre uma Intent com segurança (ignora se não houver app para tratar). */
private fun android.content.Context.abrir(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(this, "Nenhum app disponível", Toast.LENGTH_SHORT).show()
    }
}

/* ---------------------------- Formatação ---------------------------- */

private fun formatarBRL(valor: Double?): String? =
    valor?.let { "R$ %,.2f".format(Locale.forLanguageTag("pt-BR"), it) }

private fun formatarNota(nota: Double): String =
    if (nota % 1.0 == 0.0) nota.toInt().toString() else "%.1f".format(nota).replace('.', ',')

private fun dataHora(iso: String?): LocalDateTime? =
    iso?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

private fun formatarData(iso: String?): String? = dataHora(iso)?.let {
    "%02d/%02d/%d".format(it.dayOfMonth, it.monthValue, it.year)
}

private fun formatarHora(iso: String?): String? = dataHora(iso)?.let {
    "%02d:%02d".format(it.hour, it.minute)
}

private fun formatarDataHora(iso: String?): String? {
    val data = formatarData(iso) ?: return null
    return "$data às ${formatarHora(iso)}"
}

private fun faixaDeHorario(inicioIso: String?, fimIso: String?): String? {
    val inicio = formatarHora(inicioIso) ?: return null
    val fim = formatarHora(fimIso) ?: return inicio
    return "$inicio – $fim"
}

private fun rotuloTipoServico(tipo: String?): String? = when (tipo?.trim()?.lowercase()) {
    null, "" -> null
    "presencial" -> "Presencial"
    "remoto" -> "Remoto"
    else -> tipo.replace('_', ' ').replaceFirstChar { it.uppercaseChar() }
}

/** "Rua X, 10 - compl. · Bairro, Cidade - UF" com as partes que vieram preenchidas. */
private fun enderecoCompleto(endereco: OrcamentoDetalheRS.EnderecoDetalheRS?): String? {
    if (endereco == null) return null
    val logradouro = listOfNotNull(
        endereco.rua?.takeIf { it.isNotBlank() },
        endereco.numero?.toString(),
        endereco.complemento?.takeIf { it.isNotBlank() },
    ).joinToString(", ")
    val cidade = listOfNotNull(
        endereco.bairro?.takeIf { it.isNotBlank() },
        endereco.cidade?.takeIf { it.isNotBlank() },
    ).joinToString(", ")
    val estado = endereco.estado?.takeIf { it.isNotBlank() }?.let { " - $it" }.orEmpty()
    val texto = listOf(logradouro, cidade + estado).filter { it.isNotBlank() }.joinToString(" · ")
    return texto.takeIf { it.isNotBlank() }
}

private fun somenteDigitos(telefone: String?): String = telefone?.filter { it.isDigit() }.orEmpty()

/** (11) 98467-5735 quando vier só com dígitos; caso contrário devolve como está. */
private fun formatarTelefone(telefone: String): String {
    val digitos = somenteDigitos(telefone)
    return when (digitos.length) {
        11 -> "(${digitos.take(2)}) ${digitos.substring(2, 7)}-${digitos.substring(7)}"
        10 -> "(${digitos.take(2)}) ${digitos.substring(2, 6)}-${digitos.substring(6)}"
        else -> telefone
    }
}
