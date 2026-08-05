package com.winyc.elo.backend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.winyc.elo.backend.controller.orcamento.OrcamentoRepository
import com.winyc.elo.backend.controller.usuario.UsuarioRepository
import com.winyc.elo.backend.model.endereco.EnderecoRS
import com.winyc.elo.backend.model.orcamento.HorariosDisponiveisRS
import com.winyc.elo.backend.model.orcamento.OrcamentoCreateRQ
import com.winyc.elo.backend.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HorariosUi(
    val carregando: Boolean = false,
    val semana: HorariosDisponiveisRS? = null,
    val erro: String? = null,
    /** Só permite voltar quando a semana exibida está no futuro. */
    val podeVoltarSemana: Boolean = false,
)

data class EnderecosUi(
    val carregando: Boolean = false,
    val enderecos: List<EnderecoRS> = emptyList(),
    val erro: String? = null,
)

data class EnvioOrcamentoUi(
    val enviando: Boolean = false,
    val sucesso: Boolean = false,
    val erro: String? = null,
)

class OrcamentoViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore.getInstance(application)
    private val repository = OrcamentoRepository(tokenStore)
    private val usuarioRepository = UsuarioRepository(tokenStore)

    private val _horarios = MutableStateFlow(HorariosUi())
    val horarios: StateFlow<HorariosUi> = _horarios.asStateFlow()

    private val _enderecos = MutableStateFlow(EnderecosUi())
    val enderecos: StateFlow<EnderecosUi> = _enderecos.asStateFlow()

    private val _envio = MutableStateFlow(EnvioOrcamentoUi())
    val envio: StateFlow<EnvioOrcamentoUi> = _envio.asStateFlow()

    private var servicoId: Long = -1L

    /** Primeira carga da semana atual. Idempotente para o mesmo serviço. */
    fun iniciar(servicoId: Long) {
        if (servicoId <= 0) return
        if (this.servicoId == servicoId && _horarios.value.semana != null) return
        this.servicoId = servicoId
        carregarSemana(dataReferencia = null)
        if (_enderecos.value.enderecos.isEmpty()) carregarEnderecos()
    }

    fun tentarNovamenteHorarios() = carregarSemana(dataReferencia = null)

    fun proximaSemana() {
        val inicio = semanaSeguinte() ?: return
        carregarSemana(dataReferencia = inicio.toString())
    }

    fun semanaAnterior() {
        if (!_horarios.value.podeVoltarSemana) return
        val inicio = inicioSemanaAtual()?.minusWeeks(1) ?: return
        carregarSemana(dataReferencia = inicio.toString())
    }

    fun carregarEnderecos() {
        if (_enderecos.value.carregando) return
        _enderecos.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            usuarioRepository.listarEnderecos()
                .onSuccess { lista -> _enderecos.update { it.copy(enderecos = lista, carregando = false, erro = null) } }
                .onFailure { erro -> _enderecos.update { it.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO) } }
        }
    }

    fun solicitar(
        descricao: String,
        dtPreferidoSolicitado: String,
        idEndereco: Long?,
        chavesImagens: List<String> = emptyList(),
    ) {
        if (_envio.value.enviando || servicoId <= 0) return
        _envio.update { it.copy(enviando = true, erro = null) }
        viewModelScope.launch {
            repository.solicitarOrcamento(
                OrcamentoCreateRQ(
                    idServico = servicoId,
                    descricao = descricao.trim(),
                    chavesImagens = chavesImagens,
                    dtPreferidoSolicitado = dtPreferidoSolicitado,
                    idEndereco = idEndereco,
                ),
            ).onSuccess { _envio.update { it.copy(enviando = false, sucesso = true, erro = null) } }
                .onFailure { erro -> _envio.update { it.copy(enviando = false, erro = erro.message ?: ERRO_GENERICO) } }
        }
    }

    fun limparErroEnvio() = _envio.update { it.copy(erro = null) }

    private fun carregarSemana(dataReferencia: String?) {
        if (servicoId <= 0 || _horarios.value.carregando) return
        _horarios.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            repository.buscarHorariosDisponiveis(servicoId, dataReferencia)
                .onSuccess { rs ->
                    _horarios.update {
                        it.copy(
                            semana = rs,
                            carregando = false,
                            erro = null,
                            podeVoltarSemana = semanaNoFuturo(rs.inicioSemana),
                        )
                    }
                }
                .onFailure { erro -> _horarios.update { it.copy(carregando = false, erro = erro.message ?: ERRO_GENERICO) } }
        }
    }

    private fun semanaSeguinte(): LocalDate? =
        inicioSemanaAtual()?.plusWeeks(1)

    private fun inicioSemanaAtual(): LocalDate? =
        _horarios.value.semana?.inicioSemana?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    /** A semana está no futuro quando começa depois de hoje (permite voltar). */
    private fun semanaNoFuturo(inicioSemana: String?): Boolean {
        val inicio = inicioSemana?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return false
        return inicio.isAfter(LocalDate.now())
    }

    private companion object {
        const val ERRO_GENERICO = "Algo deu errado. Tente novamente."
    }
}
