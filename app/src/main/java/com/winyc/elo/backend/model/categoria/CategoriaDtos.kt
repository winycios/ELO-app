package com.winyc.elo.backend.model.categoria

data class CategoriaRS(
    val categoriaGeral: String,
    val categoriaEspecificaList: List<CategoriaEspecifica>
)

data class CategoriaEspecifica(
    val id: Long,
    val nmCategoria: String,
    val categoriaGeral: CategoriaGeral
)

data class CategoriaGeral(
    val id: Long,
    val nmCategoria: String,
    val dsIcon: String
)