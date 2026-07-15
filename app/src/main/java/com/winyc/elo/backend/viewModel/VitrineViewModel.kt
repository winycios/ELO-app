package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.vitrine.VitrineRepository
import com.winyc.elo.backend.model.vitrine.ComentarioRS
import com.winyc.elo.backend.model.vitrine.PublicacaoFeedRS
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Chip de categoria montado a partir das publicações já vistas no feed. */
data class CategoriaChip(val id: Long, val nome: String)

/** Estado do feed da vitrine, observado pela UI. */
data class VitrineUi(
    val posts: List<PublicacaoFeedRS> = emptyList(),
    val categorias: List<CategoriaChip> = emptyList(),
    val categoriaSelecionada: Long? = null,
    val carregandoInicial: Boolean = false,
    val carregandoMais: Boolean = false,
    val hasNext: Boolean = true,
    val erro: String? = null,
) {
    /** Ainda dá para buscar outra página e não há requisição em andamento. */
    val podeCarregarMais: Boolean
        get() = hasNext && !carregandoMais && !carregandoInicial
}

/** Estado dos comentários do post cuja folha está aberta (null quando fechada). */
data class ComentariosUi(
    val publicacaoId: Long,
    val comentarios: List<ComentarioRS> = emptyList(),
    val carregando: Boolean = false,
    val carregandoMais: Boolean = false,
    val enviando: Boolean = false,
    val hasNext: Boolean = true,
    val erro: String? = null,
) {
    val podeCarregarMais: Boolean
        get() = hasNext && !carregandoMais && !carregando
}

class VitrineViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = VitrineRepository(tokenStore)

    private val _estado = MutableStateFlow(VitrineUi())
    val estado: StateFlow<VitrineUi> = _estado.asStateFlow()

    private val _comentarios = MutableStateFlow<ComentariosUi?>(null)
    val comentarios: StateFlow<ComentariosUi?> = _comentarios.asStateFlow()

    // Cursores mantidos fora do estado de UI: só interessam para a próxima chamada.
    private var cursorFeed: String? = null
    private var cursorComentarios: String? = null

    init {
        carregarInicial()
    }
    fun carregarInicial() {
        if (_estado.value.carregandoInicial) return
        cursorFeed = null
        _estado.update { it.copy(carregandoInicial = true, erro = null) }
        viewModelScope.launch {
            repository.listarFeed(_estado.value.categoriaSelecionada, cursor = null)
                .onSuccess { pagina ->
                    _estado.update { atual ->
                        atual.copy(
                            posts = pagina.items,
                            categorias = mesclarCategorias(atual.categorias, pagina.items),
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

    /** Busca a próxima página; chamada quando o scroll chega perto do fim. */
    fun carregarMais() {
        val atual = _estado.value
        if (!atual.podeCarregarMais || cursorFeed == null) return
        _estado.update { it.copy(carregandoMais = true) }
        viewModelScope.launch {
            repository.listarFeed(atual.categoriaSelecionada, cursorFeed)
                .onSuccess { pagina ->
                    _estado.update { estadoAtual ->
                        estadoAtual.copy(
                            posts = estadoAtual.posts + pagina.items,
                            categorias = mesclarCategorias(estadoAtual.categorias, pagina.items),
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

    /** Troca a categoria do filtro (null = Todos) e recarrega o feed. */
    fun selecionarCategoria(categoriaId: Long?) {
        if (_estado.value.categoriaSelecionada == categoriaId) return
        _estado.update { it.copy(categoriaSelecionada = categoriaId, posts = emptyList(), hasNext = true) }
        carregarInicial()
    }

    fun limparErro() {
        _estado.update { it.copy(erro = null) }
    }

    /**
     * Alterna a curtida de forma otimista e desfaz caso o backend recuse. Só deve
     * ser chamada com usuário logado (a UI bloqueia o deslogado antes).
     */
    fun alternarCurtida(publicacaoId: Long) {
        val alvo = _estado.value.posts.firstOrNull { it.id == publicacaoId } ?: return
        val vaiCurtir = !alvo.isCurtido

        aplicarCurtida(publicacaoId, vaiCurtir)
        viewModelScope.launch {
            val resultado = if (vaiCurtir) repository.curtir(publicacaoId) else repository.descurtir(publicacaoId)
            resultado.onFailure { erro ->
                aplicarCurtida(publicacaoId, !vaiCurtir) // desfaz
                _estado.update { it.copy(erro = erro.message ?: ERRO_GENERICO) }
            }
        }
    }

    private fun aplicarCurtida(publicacaoId: Long, curtido: Boolean) {
        _estado.update { atual ->
            atual.copy(
                posts = atual.posts.map { post ->
                    if (post.id != publicacaoId || post.isCurtido == curtido) {
                        post
                    } else {
                        val delta = if (curtido) 1 else -1
                        post.copy(
                            isCurtido = curtido,
                            quantidadeCurtidas = (post.quantidadeCurtidas + delta).coerceAtLeast(0),
                        )
                    }
                },
            )
        }
    }

    // ----- Comentários -----

    fun abrirComentarios(publicacaoId: Long) {
        cursorComentarios = null
        _comentarios.value = ComentariosUi(publicacaoId = publicacaoId, carregando = true)
        viewModelScope.launch {
            repository.listarComentarios(publicacaoId, cursor = null)
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
                    _comentarios.update {
                        it?.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO)
                    }
                }
        }
    }

    fun carregarMaisComentarios() {
        val atual = _comentarios.value ?: return
        if (!atual.podeCarregarMais || cursorComentarios == null) return
        _comentarios.update { it?.copy(carregandoMais = true) }
        viewModelScope.launch {
            repository.listarComentarios(atual.publicacaoId, cursorComentarios)
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

    /** Envia um comentário (raiz) e insere no topo da lista; incrementa o contador do post. */
    fun enviarComentario(texto: String) {
        val atual = _comentarios.value ?: return
        if (atual.enviando || texto.isBlank()) return
        val publicacaoId = atual.publicacaoId
        _comentarios.update { it?.copy(enviando = true) }
        viewModelScope.launch {
            repository.comentar(publicacaoId, texto.trim())
                .onSuccess { novo ->
                    _comentarios.update { estadoAtual ->
                        estadoAtual?.takeIf { it.publicacaoId == publicacaoId }?.copy(
                            comentarios = listOf(novo) + estadoAtual.comentarios,
                            enviando = false,
                        )
                    }
                    _estado.update { estadoAtual ->
                        estadoAtual.copy(
                            posts = estadoAtual.posts.map {
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

    /** Acumula as categorias vistas sem remover as já conhecidas de páginas anteriores. */
    private fun mesclarCategorias(
        atuais: List<CategoriaChip>,
        posts: List<PublicacaoFeedRS>,
    ): List<CategoriaChip> {
        val mapa = LinkedHashMap<Long, CategoriaChip>()
        atuais.forEach { mapa[it.id] = it }
        posts.forEach { post ->
            val id = post.categoriaId
            val nome = post.categoriaNome
            if (id != null && !nome.isNullOrBlank()) mapa.putIfAbsent(id, CategoriaChip(id, nome))
        }
        return mapa.values.toList()
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}
