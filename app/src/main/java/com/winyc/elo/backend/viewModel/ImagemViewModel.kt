package com.winyc.elo.backend.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.imagem.ImagemRepository
import com.winyc.elo.backend.model.imagem.EscopoImagem
import com.winyc.elo.backend.model.imagem.ImagemUploadRS
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImagemUi(
    val emEnvio: Int = 0,
) {
    val enviando: Boolean get() = emEnvio > 0
}

class ImagemViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = ImagemRepository(application, tokenStore)

    private val _estado = MutableStateFlow(ImagemUi())
    val estado: StateFlow<ImagemUi> = _estado.asStateFlow()

    fun enviar(escopo: EscopoImagem, uri: Uri, onResultado: (Result<ImagemUploadRS>) -> Unit) {
        _estado.update { it.copy(emEnvio = it.emEnvio + 1) }
        viewModelScope.launch {
            val resultado = repository.enviar(escopo, uri)
            _estado.update { it.copy(emEnvio = (it.emEnvio - 1).coerceAtLeast(0)) }
            onResultado(resultado)
        }
    }
}
