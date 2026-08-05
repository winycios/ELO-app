package com.winyc.elo.telas.componentes

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.winyc.elo.R
import com.winyc.elo.backend.model.imagem.EscopoImagem
import com.winyc.elo.backend.viewModel.ImagemViewModel

data class ImagemFormulario(
    val id: Long,
    val uriLocal: Uri? = null,
    val url: String? = null,
    val chave: String? = null,
    val enviando: Boolean = false,
) {
    val modelo: Any? get() = uriLocal ?: url
}

@Stable
class EstadoImagens(val maximo: Int, iniciais: List<ImagemFormulario>) {

    val itens = mutableStateListOf<ImagemFormulario>().apply { addAll(iniciais) }

    private var proximoId: Long = itens.size.toLong()

    val chaves: List<String> get() = itens.mapNotNull { it.chave }

    val enviando: Boolean get() = itens.any { it.enviando }

    val podeAdicionar: Boolean get() = itens.size < maximo

    fun adicionar(uri: Uri): Long {
        val id = proximoId++
        itens.add(ImagemFormulario(id = id, uriLocal = uri, enviando = true))
        return id
    }

    fun concluir(id: Long, chave: String, url: String?) {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return
        itens[indice] = itens[indice].copy(chave = chave, url = url, enviando = false)
    }

    fun remover(id: Long) {
        itens.removeAll { it.id == id }
    }

    fun limpar() {
        itens.clear()
    }
}

private fun salvadorImagens(maximo: Int) = listSaver<EstadoImagens, String>(
    save = { estado -> estado.itens.mapNotNull { item -> item.chave?.let { "$it\n${item.url.orEmpty()}" } } },
    restore = { salvos ->
        EstadoImagens(
            maximo,
            salvos.mapIndexed { indice, texto ->
                val partes = texto.split('\n', limit = 2)
                ImagemFormulario(
                    id = indice.toLong(),
                    url = partes.getOrNull(1)?.takeIf { it.isNotBlank() },
                    chave = partes[0],
                )
            },
        )
    },
)

/**
 * @param chaveReinicio recria o estado quando muda (ex.: ao trocar o serviço em edição).
 */
@Composable
fun rememberEstadoImagens(
    maximo: Int,
    chaveReinicio: Any? = Unit,
    iniciais: () -> List<ImagemFormulario> = { emptyList() },
): EstadoImagens = rememberSaveable(maximo, chaveReinicio, saver = salvadorImagens(maximo)) {
    EstadoImagens(maximo, iniciais())
}

@Composable
fun GradeImagens(
    estado: EstadoImagens,
    escopo: EscopoImagem,
    modifier: Modifier = Modifier,
    altura: Dp = 84.dp,
    imagemVm: ImagemViewModel = viewModel(),
) {
    val context = LocalContext.current
    val erroPadrao = stringResource(R.string.imagem_erro_envio)

    val seletor = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val id = estado.adicionar(uri)
        imagemVm.enviar(escopo, uri) { resultado ->
            val enviada = resultado.getOrNull()
            if (enviada?.chave != null) {
                estado.concluir(id, enviada.chave, enviada.url)
            } else {
                estado.remover(id)
                Toast.makeText(
                    context,
                    resultado.exceptionOrNull()?.message ?: erroPadrao,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(estado.maximo) { indice ->
            val item = estado.itens.getOrNull(indice)
            when {
                item != null -> SlotImagemPreenchido(
                    item = item,
                    altura = altura,
                    onRemover = { estado.remover(item.id) },
                    modifier = Modifier.weight(1f),
                )

                indice == estado.itens.size -> SlotImagemAdicionar(
                    altura = altura,
                    onClick = { seletor.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.weight(1f),
                )

                else -> SlotImagemVazio(altura = altura, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SeletorAvatar(
    nome: String,
    urlAtual: String?,
    tamanho: Dp,
    fonte: TextStyle,
    onChave: (String) -> Unit,
    modifier: Modifier = Modifier,
    imagemVm: ImagemViewModel = viewModel(),
) {
    val context = LocalContext.current
    val erroPadrao = stringResource(R.string.imagem_erro_envio)
    var preview by rememberSaveable { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }

    val seletor = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        preview = uri.toString()
        enviando = true
        imagemVm.enviar(EscopoImagem.PERFIL, uri) { resultado ->
            enviando = false
            val chave = resultado.getOrNull()?.chave
            if (chave != null) {
                onChave(chave)
            } else {
                preview = null
                Toast.makeText(
                    context,
                    resultado.exceptionOrNull()?.message ?: erroPadrao,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        AvatarPerfil(
            nome = nome,
            fotoUrl = preview ?: urlAtual,
            tamanho = tamanho,
            fonte = fonte,
        )
        if (enviando) {
            Box(
                modifier = Modifier
                    .size(tamanho)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(tamanho / 3),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            }
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = !enviando) {
                    seletor.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.PhotoCamera,
                stringResource(R.string.perfil_trocar_foto),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SlotImagemPreenchido(
    item: ImagemFormulario,
    altura: Dp,
    onRemover: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(altura)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = item.modelo,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (item.enviando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(onClick = onRemover),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Close,
                    stringResource(R.string.imagem_remover),
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun SlotImagemAdicionar(altura: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(altura)
            .clip(RoundedCornerShape(12.dp))
            .bordaTracejada(MaterialTheme.colorScheme.primary, 12.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.AddPhotoAlternate,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.size(4.dp))
        Text(
            stringResource(R.string.imagem_adicionar),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SlotImagemVazio(altura: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(altura)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.Image,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun Modifier.bordaTracejada(cor: Color, raio: Dp): Modifier = drawBehind {
    drawRoundRect(
        color = cor,
        cornerRadius = CornerRadius(raio.toPx()),
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
        ),
    )
}
