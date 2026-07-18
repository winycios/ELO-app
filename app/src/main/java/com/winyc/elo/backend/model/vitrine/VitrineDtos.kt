package com.winyc.elo.backend.model.vitrine

/**
 * Publicação exibida no feed da vitrine.
 *
 * As datas chegam como string ISO-8601 (ex.: `2026-07-14T10:15:30`) porque o Gson
 * compartilhado do app não tem adapter para `java.time`; a formatação em tempo
 * relativo é feita na UI.
 */
data class PublicacaoFeedRS(
    val id: Long,
    val descricao: String,
    val publicadoEm: String?,
    val categoriaId: Long?,
    val categoriaNome: String?,
    val profissionalId: Long?,
    val profissionalNome: String?,
    val profissionalFotoUrl: String?,
    val imagens: List<PublicacaoImagemRS> = emptyList(),
    val quantidadeCurtidas: Long = 0,
    val quantidadeComentarios: Long = 0,
    val isCurtido: Boolean = false,
)

data class PublicacaoImagemRS(
    val id: Long,
    val url: String,
    val ordem: Int?,
)

data class ComentarioRS(
    val id: Long,
    val texto: String,
    val comentadoEm: String?,
    val comentarioPaiId: Long?,
    val usuarioId: Long?,
    val usuarioNome: String?,
    val usuarioFotoUrl: String?,
)

data class ComentarioCreateRQ(
    val texto: String,
    val comentarioPaiId: Long? = null,
)

data class PublicacaoCreateDTO(
    val idCategoriaEspecifica: Long,
    val dsPublicacao: String,
    val publicacaoImagemDTOList: List<PublicacaoImagemDTO> = emptyList(),
)

data class PublicacaoImagemDTO(
    val urlImagem: String,
    val nrOrdem: Int,
)
