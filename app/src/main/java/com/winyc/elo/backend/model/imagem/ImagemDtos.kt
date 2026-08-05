package com.winyc.elo.backend.model.imagem

data class ImagemUploadRS(
    val chave: String? = null,
    val url: String? = null,
)

enum class EscopoImagem(val prefixo: String) {
    /** Foto de perfil do usuário e do profissional. */
    PERFIL("perfil"),

    /** Fotos dos serviços oferecidos pelo profissional. */
    SERVICO("servico"),

    /** Fotos das publicações da vitrine. */
    PUBLICACAO("publicacao"),

    /** Fotos que o cliente anexa ao solicitar um orçamento (escopo privado). */
    ORCAMENTO("orcamento"),
}
