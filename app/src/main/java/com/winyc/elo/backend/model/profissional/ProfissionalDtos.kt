package com.winyc.elo.backend.model.profissional

data class ProfissionalRS(
    val usuarioId: Long? = null,
    val qtServicos: Int? = null,
    val qtRespostaGeral: Int? = null,
    val stDisponivel: Boolean? = null,
    val apresentacao: String? = null,
    val urlPerfil: String? = null,
    val dsEspecialidades: String? = null,
    val areaAtendimentoRS: AreaAtendimentoRS? = null,
)

data class AreaAtendimentoRS(
    val id: Long? = null,
    val nrLatitude: Double? = null,
    val nrLongitude: Double? = null,
    val nrRaio: Int? = null,
    val nmCidade: String? = null,
    val nmBairro: String? = null,
    val nmEstado: String? = null,
)

data class ProfissionalUpdateRQ(
    val apresentacao: String?,
    val chaveImagem: String?,
    val especialidades: String?,
    val areaAtendimentoUpdateRQ: AreaAtendimentoUpdateRQ,
)

data class AreaAtendimentoUpdateRQ(
    val id: Long? = null,
    val nrLatitude: Double,
    val nrLongitude: Double,
    val nrRaio: Int,
    val nmCidade: String,
    val nmEstado: String,
    val nmBairro: String,
)
