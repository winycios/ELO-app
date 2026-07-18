package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.profissional.ProfissionalRepository
import com.winyc.elo.backend.controller.vitrine.VitrineRepository
import com.winyc.elo.backend.model.vitrine.ComentarioRS
import com.winyc.elo.backend.model.vitrine.PublicacaoCreateDTO
import com.winyc.elo.backend.model.vitrine.PublicacaoFeedRS
import com.winyc.elo.backend.model.vitrine.PublicacaoImagemDTO
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PublicacaoUi(
    val publicacoes: List<PublicacaoFeedRS> = emptyList(),
    val carregandoInicial: Boolean = false,
    val carregandoMais: Boolean = false,
    val publicando: Boolean = false,
    val hasNext: Boolean = true,
    val erro: String? = null,
) {
    val podeCarregarMais: Boolean
        get() = hasNext && !carregandoMais && !carregandoInicial
}

data class ComentariosPubUi(
    val publicacaoId: Long,
    val comentarios: List<ComentarioRS> = emptyList(),
    val carregando: Boolean = false,
    val carregandoMais: Boolean = false,
    val enviando: Boolean = false,
    val respondendoId: Long? = null,
    val hasNext: Boolean = true,
    val erro: String? = null,
) {
    val podeCarregarMais: Boolean
        get() = hasNext && !carregandoMais && !carregando
}

class PublicacaoViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = ProfissionalRepository(tokenStore)
    private val vitrineRepository = VitrineRepository(tokenStore)

    private val _estado = MutableStateFlow(PublicacaoUi())
    val estado: StateFlow<PublicacaoUi> = _estado.asStateFlow()

    private val _comentarios = MutableStateFlow<ComentariosPubUi?>(null)
    val comentarios: StateFlow<ComentariosPubUi?> = _comentarios.asStateFlow()

    private var cursorFeed: String? = null
    private var cursorComentarios: String? = null

    init {
        if (tokenStore.estaLogado) carregarInicial()
        observarSessao()
    }

    private fun observarSessao() {
        viewModelScope.launch {
            tokenStore.estaLogadoFlow.drop(1).collect { logado ->
                _comentarios.value = null
                cursorComentarios = null
                _estado.value = PublicacaoUi()
                cursorFeed = null
                if (logado) carregarInicial()
            }
        }
    }

    fun carregarInicial() {
        if (!tokenStore.estaLogado || _estado.value.carregandoInicial) return
        cursorFeed = null
        _estado.update { it.copy(carregandoInicial = true, erro = null) }
        viewModelScope.launch {
            repository.listarMinhasPublicacoes(categoriaId = null, cursor = null)
                .onSuccess { pagina ->
                    _estado.update {
                        it.copy(
                            publicacoes = pagina.items,
                            carregandoInicial = false,
                            hasNext = pagina.hasNext,
                            erro = null,
                        )
                    }
                    cursorFeed = pagina.nextCursor
                }
                .onFailure { erro ->
                    _estado.update { it.copy(carregandoInicial = false, erro = erro.message ?: ERRO_GENERICO) }
                }
        }
    }

    fun carregarMais() {
        val atual = _estado.value
        if (!atual.podeCarregarMais || cursorFeed == null) return
        _estado.update { it.copy(carregandoMais = true) }
        viewModelScope.launch {
            repository.listarMinhasPublicacoes(categoriaId = null, cursor = cursorFeed)
                .onSuccess { pagina ->
                    _estado.update {
                        it.copy(
                            publicacoes = it.publicacoes + pagina.items,
                            carregandoMais = false,
                            hasNext = pagina.hasNext,
                        )
                    }
                    cursorFeed = pagina.nextCursor
                }
                .onFailure { erro ->
                    _estado.update { it.copy(carregandoMais = false, erro = erro.message ?: ERRO_GENERICO) }
                }
        }
    }

    fun publicar(
        idCategoriaEspecifica: Long,
        descricao: String,
        imagens: List<PublicacaoImagemDTO> = emptyList(),
        onResultado: (Boolean) -> Unit = {},
    ) {
        if (_estado.value.publicando || descricao.isBlank()) return
        _estado.update { it.copy(publicando = true, erro = null) }
        val dto = PublicacaoCreateDTO(
            idCategoriaEspecifica = idCategoriaEspecifica,
            dsPublicacao = descricao.trim(),
            publicacaoImagemDTOList = imagens,
        )
        viewModelScope.launch {
            repository.salvarPublicacao(dto)
                .onSuccess { nova ->
                    _estado.update { it.copy(publicacoes = listOf(nova) + it.publicacoes, publicando = false) }
                    onResultado(true)
                }
                .onFailure { erro ->
                    _estado.update { it.copy(publicando = false, erro = erro.message ?: ERRO_GENERICO) }
                    onResultado(false)
                }
        }
    }

    fun excluir(id: Long) {
        _estado.update { atual -> atual.copy(publicacoes = atual.publicacoes.filterNot { it.id == id }) }
        if (_comentarios.value?.publicacaoId == id) fecharComentarios()
        viewModelScope.launch {
            repository.desativarPublicacao(id)
                .onFailure { erro ->
                    _estado.update { it.copy(erro = erro.message ?: ERRO_GENERICO) }
                    carregarInicial()
                }
        }
    }

    fun limparErro() {
        _estado.update { it.copy(erro = null) }
    }

    fun abrirComentarios(publicacaoId: Long) {
        cursorComentarios = null
        _comentarios.value = ComentariosPubUi(publicacaoId = publicacaoId, carregando = true)
        viewModelScope.launch {
            vitrineRepository.listarComentarios(publicacaoId, cursor = null)
                .onSuccess { pagina ->
                    _comentarios.update { atual ->
                        atual?.takeIf { it.publicacaoId == publicacaoId }?.copy(
                            comentarios = pagina.items,
                            carregando = false,
                            hasNext = pagina.hasNext,
                        )
                    }
                    cursorComentarios = pagina.nextCursor
                }
                .onFailure { erro ->
                    _comentarios.update { it?.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO) }
                }
        }
    }

    fun carregarMaisComentarios() {
        val atual = _comentarios.value ?: return
        if (!atual.podeCarregarMais || cursorComentarios == null) return
        _comentarios.update { it?.copy(carregandoMais = true) }
        viewModelScope.launch {
            vitrineRepository.listarComentarios(atual.publicacaoId, cursorComentarios)
                .onSuccess { pagina ->
                    _comentarios.update { estadoAtual ->
                        estadoAtual?.takeIf { it.publicacaoId == atual.publicacaoId }?.copy(
                            comentarios = estadoAtual.comentarios + pagina.items,
                            carregandoMais = false,
                            hasNext = pagina.hasNext,
                        )
                    }
                    cursorComentarios = pagina.nextCursor
                }
                .onFailure { erro ->
                    _comentarios.update { it?.copy(carregandoMais = false, erro = erro.message ?: ERRO_GENERICO) }
                }
        }
    }

    fun alternarResponder(comentarioPaiId: Long?) {
        _comentarios.update { atual ->
            atual?.copy(respondendoId = if (atual.respondendoId == comentarioPaiId) null else comentarioPaiId)
        }
    }


    fun enviarComentario(texto: String, comentarioPaiId: Long? = null) {
        val atual = _comentarios.value ?: return
        if (atual.enviando || texto.isBlank()) return
        val publicacaoId = atual.publicacaoId
        _comentarios.update { it?.copy(enviando = true) }
        viewModelScope.launch {
            vitrineRepository.comentar(publicacaoId, texto.trim(), comentarioPaiId)
                .onSuccess { novo ->
                    _comentarios.update { estadoAtual ->
                        estadoAtual?.takeIf { it.publicacaoId == publicacaoId }?.copy(
                            comentarios = estadoAtual.comentarios + novo,
                            enviando = false,
                            respondendoId = null,
                        )
                    }
                    _estado.update { estadoAtual ->
                        estadoAtual.copy(
                            publicacoes = estadoAtual.publicacoes.map {
                                if (it.id == publicacaoId) it.copy(quantidadeComentarios = it.quantidadeComentarios + 1) else it
                            },
                        )
                    }
                }
                .onFailure { erro ->
                    _comentarios.update { it?.copy(enviando = false, erro = erro.message ?: ERRO_GENERICO) }
                }
        }
    }

    fun limparErroComentarios() {
        _comentarios.update { it?.copy(erro = null) }
    }

    fun fecharComentarios() {
        _comentarios.value = null
        cursorComentarios = null
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}
