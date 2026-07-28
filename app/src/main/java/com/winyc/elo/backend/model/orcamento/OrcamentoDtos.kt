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

data class OrcamentoListagemRS(
    val id: Long,
    val idServico: Long? = null,
    val idProfissional: Long? = null,
    /** Preenchido quando o profissional já enviou o orçamento final. */
    val orcamentoFinalId: Long? = null,
    val nomeProfissional: String? = null,
    val fotoProfissional: String? = null,
    val categoria: String? = null,
    val descricao: String? = null,
    val status: String? = null,
)

data class OrcamentoDetalheRS(
    val id: Long,
    val status: String? = null,
    val profissional: ProfissionalOrcamentoRS? = null,
    val solicitacao: SolicitacaoOrcamentoRS? = null,
    val orcamentoFinal: OrcamentoFinalRS? = null,
) {
    data class ProfissionalOrcamentoRS(
        val id: Long? = null,
        val nome: String? = null,
        val fotoPerfil: String? = null,
        val categoria: String? = null,
        val avaliacao: Double? = null,
        val quantidadeAvaliacoes: Int? = null,
        val verificado: Boolean? = null,
        val contato: ContatoProfissionalRS? = null,
    )

    data class ContatoProfissionalRS(
        val telefone: String? = null,
        val whatsapp: String? = null,
    )

    data class SolicitacaoOrcamentoRS(
        val idServico: Long? = null,
        val idCategoria: Long? = null,
        val categoria: String? = null,
        val descricao: String? = null,
        val tipoServico: String? = null,
        val horarioPreferido: String? = null,
        val valor: Double? = null,
        val imagens: List<String> = emptyList(),
        val endereco: EnderecoDetalheRS? = null,
    )

    data class OrcamentoFinalRS(
        val id: Long? = null,
        val inicioProposto: String? = null,
        val fimProposto: String? = null,
        val observacaoProfissional: String? = null,
        val custos: List<CustoOrcamentoRS> = emptyList(),
        val valorTotal: Double? = null,
    )

    data class CustoOrcamentoRS(
        val id: Long? = null,
        val descricao: String? = null,
        val valor: Double? = null,
    )

    data class EnderecoDetalheRS(
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