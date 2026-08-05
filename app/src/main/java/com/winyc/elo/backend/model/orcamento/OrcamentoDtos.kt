package com.winyc.elo.backend.model.orcamento

import androidx.compose.ui.graphics.vector.ImageVector

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
    /** Até 3 chaves devolvidas pelo upload no escopo `orcamento`. */
    val chavesImagens: List<String> = emptyList(),
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
    /** Já concluído e avaliado por este usuário. */
    val avaliado: Boolean? = null,
)

data class OrcamentoDetalheRS(
    val id: Long,
    val status: String? = null,
    val profissional: ProfissionalOrcamentoRS? = null,
    val solicitacao: SolicitacaoOrcamentoRS? = null,
    val orcamentoFinal: OrcamentoFinalRS? = null,
    val cancelamento: CancelamentoRS? = null,
    val conclusao: ConclusaoRS? = null,
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

    data class CancelamentoRS(
        val autor: String? = null,
        val idUsuario: Long? = null,
        val motivo: String? = null,
        val descricao: String? = null,
        val data: String? = null,
    )

    data class ConclusaoRS(
        val idProfissional: Long? = null,
        val observacao: String? = null,
        val data: String? = null,
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


data class OrcamentoListagemProfissionalRS(
    val id: Long,
    val idServico: Long? = null,
    val nomeUsuario: String? = null,
    val fotoUsuario: String? = null,
    val avaliacaoUsuario: Double? = null,
    val categoria: String? = null,
    val descricao: String? = null,
    val distanciaKm: Double? = null,
    val bairro: String? = null,
    val dataHoraCriacao: String? = null,
    val horarioPreferido: String? = null,
    val inicioProposto: String? = null,
    val fimProposto: String? = null,
    val valorTotal: Double? = null,
    val status: String? = null,
    /** Já concluído e avaliado por este profissional. */
    val avaliado: Boolean? = null,
)

data class OrcamentoDetalheProfissionalRS(
    val id: Long,
    val status: String? = null,
    val cliente: ClienteOrcamentoRS? = null,
    val solicitacao: SolicitacaoProfissionalRS? = null,
    val orcamentoFinal: OrcamentoDetalheRS.OrcamentoFinalRS? = null,
    val cancelamento: OrcamentoDetalheRS.CancelamentoRS? = null,
    val conclusao: OrcamentoDetalheRS.ConclusaoRS? = null,
) {
    data class ClienteOrcamentoRS(
        val id: Long? = null,
        val nome: String? = null,
        val fotoPerfil: String? = null,
        val avaliacao: Double? = null,
        val quantidadeAvaliacoes: Int? = null,
        val habilitado: Boolean? = null,
        val contato: ContatoClienteRS? = null,
    )

    data class ContatoClienteRS(
        val telefone: String? = null,
        val whatsapp: String? = null,
    )

    data class SolicitacaoProfissionalRS(
        val idServico: Long? = null,
        val idCategoria: Long? = null,
        val categoria: String? = null,
        val descricao: String? = null,
        val tipoServico: String? = null,
        val horarioPreferido: String? = null,
        val distanciaKm: Double? = null,
        val imagens: List<String> = emptyList(),
        val endereco: OrcamentoDetalheRS.EnderecoDetalheRS? = null,
    )
}

data class OrcamentoFinalCreateRQ(
    val inicioProposto: String,
    val fimProposto: String,
    val observacaoProfissional: String? = null,
    val custos: List<CustoRQ>,
) {
    data class CustoRQ(
        val descricao: String,
        val valor: Double,
    )
}

data class OrcamentoCancelamentoRQ(
    val motivo: String,
    val descricao: String,
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

data class ItemCusto(val nome: String, val icone: ImageVector, val valor: String = "")

data class CategoriaCusto(val nome: String, val icone: ImageVector)
data class OrcamentoConclusaoRQ(
    val observacao: String? = null,
)

data class AvaliacaoOrcamentoRQ(
    val nota: Int,
    val comentario: String? = null,
)

data class AvaliacaoOrcamentoRS(
    val id: Long? = null,
    val nota: Int? = null,
    val comentario: String? = null,
    val data: String? = null,
)
