package com.winyc.elo.backend.model.endereco

data class EnderecoRS(
    val id: Long,
    val nmApelido: String? = null,
    val tipoEndereco: String? = null,
    val cep: String? = null,
    val rua: String? = null,
    val nrRua: Int? = null,
    val complemento: String? = null,
    val bairro: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val stPrincipal: Boolean? = null,
)

fun EnderecoRS.linhaEndereco(): String {

    val ruaFormatada = rua
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    val numeroFormatado = nrRua
        ?.takeIf { it > 0 }
        ?.toString()

    val regiao = listOfNotNull(
        bairro?.trim()?.takeIf { it.isNotEmpty() },
        cidade?.trim()?.takeIf { it.isNotEmpty() },
        estado?.trim()?.takeIf { it.isNotEmpty() }
    ).joinToString(" - ")

    return listOfNotNull(
        ruaFormatada,
        numeroFormatado,
        regiao.takeIf { it.isNotEmpty() }
    ).joinToString(" • ")
}

data class EnderecoCreateRQ(
    val id: Long?,
    val nmApelido: String,
    val tipoEndereco: String,
    val cep: String,
    val rua: String,
    val nrRua: Int,
    val complemento: String,
    val bairro: String,
    val cidade: String,
    val estado: String,
)
