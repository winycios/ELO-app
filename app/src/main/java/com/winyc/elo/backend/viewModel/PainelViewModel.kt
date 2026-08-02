package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.profissional.ProfissionalRepository
import com.winyc.elo.backend.model.profissional.ProfissionalDashboardRS
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PainelUi(
    val dashboard: ProfissionalDashboardRS? = null,
    val carregando: Boolean = false,
    val erro: String? = null,
)

class PainelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfissionalRepository(TokenStore.getInstance(application))

    private val _estado = MutableStateFlow(PainelUi())
    val estado: StateFlow<PainelUi> = _estado.asStateFlow()

    private var busca: Job? = null

    fun carregar() {
        if (busca?.isActive == true) return
        _estado.update { it.copy(carregando = true, erro = null) }
        busca = viewModelScope.launch {
            repository.buscarDashboard()
                .onSuccess { rs ->
                    _estado.update { it.copy(dashboard = rs, carregando = false, erro = null) }
                }
                .onFailure { erro ->
                    _estado.update {
                        it.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO)
                    }
                }
        }
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}
