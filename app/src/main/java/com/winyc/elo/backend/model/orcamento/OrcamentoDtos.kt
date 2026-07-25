package com.winyc.elo.backend.model.orcamento

data class HorariosDisponiveisRS(
    val idServico: Long? = null,
    val inicioSemana: String? = null,
    val fimSemana: String? = null,
    val intervaloMinutos: Int? = null,
    val margemAposReservaMinutos: Int? = null,
    val dias: List<DiaHorariosRS> = emptyList(),
)

data class DiaHorariosRS(
    val data: String? = null,
    val diaSemana: Int? = null,
    val expediente: List<FaixaTrabalhoRS> = emptyList(),
    val horariosDisponiveis: List<String> = emptyList(),
)

data class FaixaTrabalhoRS(
    val inicio: String? = null,
    val fim: String? = null,
)

data class OrcamentoCreateRQ(
    val idServico: Long,
    val descricao: String,
    val orcamentoImagemCreateRQList: List<String> = emptyList(),
    /** Data/hora preferida em ISO local, ex.: `2026-07-26T09:00`*/
    val dtPreferidoSolicitado: String,
    val idEndereco: Long? = null,
)

data class OrcamentoRS(
    val id: Long,
    val idServico: Long? = null,
    val idProfissional: Long? = null,
    val idCliente: Long? = null,
    val status: String? = null,
    val descricao: String? = null,
    val horarioPreferido: String? = null,
    val inicioProposto: String? = null,
    val fimProposto: String? = null,
    val imagens: List<String> = emptyList(),
    val endereco: EnderecoOrcamentoRS? = null,
) {
    data class EnderecoOrcamentoRS(
        val rua: String? = null,
        val numero: Int? = null,
        val complemento: String? = null,
        val bairro: String? = null,
        val cidade: String? = null,
        val estado: String? = null,
        val cep: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
    )
}