package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.categoria.CategoriaRepository
import com.winyc.elo.backend.model.categoria.CategoriaRS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Categoria geral com seus serviços específicos, já pronta para a UI. */
data class CategoriaUi(
    val nomeGeral: String,
    val descricaoIcon: String,
    val servicos: List<String>,
)

/** Estado da lista de categorias exibida na home do cliente. */
data class CategoriasUi(
    val categorias: List<CategoriaUi> = emptyList(),
    val categoriasRaw: List<CategoriaRS> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null,
)

class CategoriaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CategoriaRepository()

    private val _estado = MutableStateFlow(CategoriasUi())
    val estado: StateFlow<CategoriasUi> = _estado.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        if (_estado.value.carregando) return
        _estado.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            repository.listarCategorias()
                .onSuccess { lista ->
                    _estado.update {
                        it.copy(
                            categoriasRaw = lista,
                            categorias = lista.map { categoria ->
                                CategoriaUi(
                                    nomeGeral = categoria.categoriaGeral,
                                    descricaoIcon = categoria.categoriaEspecificaList.find { e -> e.categoriaGeral.nmCategoria == categoria.categoriaGeral }?.categoriaGeral?.dsIcon ?: "Work",
                                    servicos = categoria.categoriaEspecificaList.map { e -> e.nmCategoria },
                                )
                            },
                            carregando = false,
                            erro = null,
                        )
                    }
                }
                .onFailure { erro ->
                    _estado.update { it.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO) }
                }
        }
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}
