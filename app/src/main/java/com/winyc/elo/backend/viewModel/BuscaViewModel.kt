package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.busca.BuscaRepository
import com.winyc.elo.backend.model.search.OrdenacaoBusca
import com.winyc.elo.backend.model.search.ProfissionalBuscaRS
import com.winyc.elo.backend.model.search.SelosDaBusca
import com.winyc.elo.backend.model.search.selosDaLista
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Localização usada nas buscas; nula quando o cliente está deslogado. */
data class Localizacao(val latitude: Double?, val longitude: Double?) {
    companion object {
        val NENHUMA = Localizacao(null, null)
    }
}

/** Carrosséis da home ("Recomendados" e "Em alta"). */
data class BuscaHomeUi(
    val recomendados: List<ProfissionalBuscaRS> = emptyList(),
    val emAlta: List<ProfissionalBuscaRS> = emptyList(),
    val selos: SelosDaBusca = SelosDaBusca.NENHUM,
    val carregando: Boolean = false,
    val atualizando: Boolean = false,
    val erro: String? = null,
)

/** Tela de uma categoria: lista paginada com filtros de ordenação/avaliação. */
data class BuscaCategoriaUi(
    val categoriaId: Long? = null,
    val titulo: String = "",
    val texto: String? = null,
    val profissionais: List<ProfissionalBuscaRS> = emptyList(),
    val selos: SelosDaBusca = SelosDaBusca.NENHUM,
    val ordenacao: OrdenacaoBusca = OrdenacaoBusca.RECOMENDADOS,
    val avaliacaoMinima: Double = 0.0,
    val pagina: Int = 0,
    val total: Long = 0,
    val temMais: Boolean = false,
    val carregando: Boolean = false,
    val carregandoMais: Boolean = false,
    val erro: String? = null,
)

class BuscaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BuscaRepository()

    private val _home = MutableStateFlow(BuscaHomeUi())
    val home: StateFlow<BuscaHomeUi> = _home.asStateFlow()

    private val _categoria = MutableStateFlow(BuscaCategoriaUi())
    val categoria: StateFlow<BuscaCategoriaUi> = _categoria.asStateFlow()

    private var localizacao = Localizacao.NENHUMA
    private var homeCarregadaCom: Localizacao? = null

    /**
     * Carrega os carrosséis da home. Só refaz a chamada quando a localização
     * muda (ex.: usuário logou e o endereço principal chegou depois).
     */
    fun carregarHome(loc: Localizacao) {
        localizacao = loc
        if (_home.value.carregando) return
        if (homeCarregadaCom == loc && _home.value.erro == null && _home.value.recomendados.isNotEmpty()) return
        homeCarregadaCom = loc
        executarBuscaHome(loc)
    }

    fun recarregarHome() {
        if (_home.value.carregando) return
        homeCarregadaCom = null
        _home.update { it.copy(atualizando = true) }
        executarBuscaHome(localizacao)
    }

    private fun executarBuscaHome(loc: Localizacao) {
        _home.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            val recomendados = async {
                repository.buscar(ordenacao = OrdenacaoBusca.RECOMENDADOS, latitude = loc.latitude, longitude = loc.longitude, tamanho = 10)
            }
            val emAlta = async {
                repository.buscar(ordenacao = OrdenacaoBusca.AVALIACAO, latitude = loc.latitude, longitude = loc.longitude, tamanho = 10)
            }
            val resRecomendados = recomendados.await()
            val resEmAlta = emAlta.await()
            _home.update { atual ->
                val recomendadosNovos =
                    resRecomendados.getOrNull()?.profissionais ?: atual.recomendados
                atual.copy(
                    recomendados = recomendadosNovos,
                    emAlta = resEmAlta.getOrNull()?.profissionais ?: atual.emAlta,
                    selos = selosDaLista(recomendadosNovos),
                    carregando = false,
                    atualizando = false,
                    erro = (resRecomendados.exceptionOrNull() ?: resEmAlta.exceptionOrNull())?.message,
                )
            }
        }
    }

    /** Abre uma categoria (tile) ou um serviço específico (texto). */
    fun abrirCategoria(categoriaId: Long?, titulo: String, texto: String?) {
        _categoria.value = BuscaCategoriaUi(categoriaId = categoriaId, titulo = titulo, texto = texto)
        buscarCategoria(pagina = 0)
    }

    fun mudarOrdenacao(ordenacao: OrdenacaoBusca) {
        if (_categoria.value.ordenacao == ordenacao) return
        _categoria.update { it.copy(ordenacao = ordenacao) }
        buscarCategoria(pagina = 0)
    }

    fun mudarAvaliacaoMinima(minimo: Double) {
        if (_categoria.value.avaliacaoMinima == minimo) return
        _categoria.update { it.copy(avaliacaoMinima = minimo) }
        buscarCategoria(pagina = 0)
    }

    fun tentarNovamenteCategoria() = buscarCategoria(pagina = 0)

    fun carregarMais() {
        val estado = _categoria.value
        if (!estado.temMais || estado.carregando || estado.carregandoMais) return
        buscarCategoria(pagina = estado.pagina + 1)
    }

    private fun buscarCategoria(pagina: Int) {
        val estado = _categoria.value
        val primeiraPagina = pagina == 0
        _categoria.update {
            it.copy(
                carregando = primeiraPagina,
                carregandoMais = !primeiraPagina,
                erro = null,
            )
        }
        viewModelScope.launch {
            repository.buscar(
                texto = estado.texto,
                categoriaId = estado.categoriaId,
                avaliacaoMinima = estado.avaliacaoMinima,
                latitude = localizacao.latitude,
                longitude = localizacao.longitude,
                ordenacao = estado.ordenacao,
                pagina = pagina,
            ).onSuccess { rs ->
                _categoria.update { atual ->
                    val lista = if (primeiraPagina) rs.profissionais else atual.profissionais + rs.profissionais
                    val anteriores = if (primeiraPagina) SelosDaBusca.NENHUM else atual.selos
                    atual.copy(
                        profissionais = lista,
                        selos = selosDaLista(lista, anteriores),
                        pagina = rs.pagina,
                        total = rs.total,
                        temMais = lista.size < rs.total,
                        carregando = false,
                        carregandoMais = false,
                        erro = null,
                    )
                }
            }.onFailure { erro ->
                _categoria.update {
                    it.copy(carregando = false, carregandoMais = false, erro = erro.message ?: ERRO_GENERICO)
                }
            }
        }
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}
