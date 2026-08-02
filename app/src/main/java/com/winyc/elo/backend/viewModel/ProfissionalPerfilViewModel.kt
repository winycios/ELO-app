package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.estimativa.EstimativaRepository
import com.winyc.elo.backend.model.estimativa.AvaliacaoRS
import com.winyc.elo.backend.model.estimativa.ProfissionalServicoRS
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfissionalPerfilUi(
    val perfil: ProfissionalServicoRS? = null,
    val carregando: Boolean = false,
    val erro: String? = null,
)

/** Lista completa de comentários (tela "todas as avaliações"). */
data class ComentariosAvaliacaoUi(
    val avaliacoes: List<AvaliacaoRS> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null,
)

/**
 * Carrega os detalhes de um profissional para estimar um orçamento. A origem
 * define como buscar: pelo serviço (busca) ou pela categoria (vitrine).
 */
class ProfissionalPerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EstimativaRepository(TokenStore.getInstance(application))

    private val _estado = MutableStateFlow(ProfissionalPerfilUi())
    val estado: StateFlow<ProfissionalPerfilUi> = _estado.asStateFlow()

    private val _comentarios = MutableStateFlow(ComentariosAvaliacaoUi())
    val comentarios: StateFlow<ComentariosAvaliacaoUi> = _comentarios.asStateFlow()

    private var ultimaBusca: (suspend () -> Result<ProfissionalServicoRS>)? = null
    private var comentariosCarregados: Pair<Long, Long>? = null

    fun carregarPorServico(profissionalId: Long, servicoId: Long) =
        carregar { repository.buscarPorServico(profissionalId, servicoId) }

    fun carregarPorCategoria(profissionalId: Long, categoriaId: Long) =
        carregar { repository.buscarPorCategoria(profissionalId, categoriaId) }

    fun tentarNovamente() {
        ultimaBusca?.let { carregar(it) }
    }

    /**
     * Busca todos os comentários da categoria geral. Só refaz a chamada quando o
     * par profissional/categoria muda ou quando a anterior falhou.
     */
    fun carregarComentarios(profissionalId: Long, categoriaGeralId: Long, forcar: Boolean = false) {
        val chave = profissionalId to categoriaGeralId
        val estado = _comentarios.value
        if (estado.carregando) return
        if (!forcar && comentariosCarregados == chave && estado.erro == null) return

        if (comentariosCarregados != chave) _comentarios.value = ComentariosAvaliacaoUi()
        _comentarios.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            repository.buscarComentarios(profissionalId, categoriaGeralId)
                .onSuccess { lista ->
                    comentariosCarregados = chave
                    _comentarios.value = ComentariosAvaliacaoUi(avaliacoes = lista)
                }
                .onFailure { erro ->
                    comentariosCarregados = null
                    _comentarios.update {
                        it.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO)
                    }
                }
        }
    }

    private fun carregar(busca: suspend () -> Result<ProfissionalServicoRS>) {
        ultimaBusca = busca
        if (_estado.value.carregando) return
        _estado.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            busca()
                .onSuccess { rs -> _estado.update { it.copy(perfil = rs, carregando = false, erro = null) } }
                .onFailure { erro -> _estado.update { it.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO) } }
        }
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}
