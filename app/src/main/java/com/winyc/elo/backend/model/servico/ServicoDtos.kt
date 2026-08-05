package com.winyc.elo.backend.model.servico

data class ServicoRS(
    val id: Long,
    val idProfissional: Long? = null,
    val servicoCategoriaRS: ServicoCategoriaRS? = null,
    val servicoImagemRSList: List<ServicoImagemRS> = emptyList(),
    val servicoDisponibilidadeRSList: List<ServicoDisponibilidadeRS> = emptyList(),
    val dsDescricao: String? = null,
    val vlServico: Double? = null,
    val tempoExperiencia: Int? = null,
    val dsTag: String? = null,
    val tpExecucao: String? = null,
)

data class ServicoListaRS(
    val id: Long,
    val categoria: ServicoCategoriaRS? = null,
    val dsDescricao: String? = null,
    val vlServico: Double? = null,
    val dsTag: String? = null,
    val tpExecucao: String? = null,
)

data class ServicoCategoriaRS(
    val idCategoriaEspecifica: Long? = null,
    val idCategoriaGeral: Long? = null,
)

data class ServicoImagemRS(
    val idServicoImagem: Long? = null,
    val chave: String? = null,
    val url: String? = null,
    val ordem: Int? = null,
)

data class ServicoDisponibilidadeRS(
    val idServicoDisponibilidade: Long? = null,
    val diaSemana: Int? = null,
    val hrInicio: String? = null,
    val hrFim: String? = null,
)

data class ServicoCreateRQ(
    val id: Long? = null,
    val idCategoriaEspecifica: Long,
    val dsDescricao: String,
    val vlServico: Double,
    val dsTag: String,
    val tempoExperiencia: Int,
    val tpExecucao: String,
    val servicoDisponibilidadeCreateRQList: List<ServicoDisponibilidadeCreateRQ> = emptyList(),
    val servicoImagemCreateRQList: List<ServicoImagemCreateRQ> = emptyList(),
)

data class ServicoDisponibilidadeCreateRQ(
    val id: Long? = null,
    val diaSemana: Int,
    val hrInicio: String,
    val hrFim: String,
)

data class ServicoImagemCreateRQ(
    val id: Long? = null,
    val chaveImagem: String,
    val ordem: Int,
)
