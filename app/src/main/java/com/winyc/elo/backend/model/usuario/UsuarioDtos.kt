package com.winyc.elo.backend.model.usuario

data class UsuarioRS(
    val id: Long? = null,
    val nome: String? = null,
    val email: String? = null,
    val telefone: String? = null,
    val telefoneZap: String? = null,
    val qtdPedido: Long? = null,
    val qtdConcluido: Long? = null,
    val avaliacaoGeral: Double? = null,
)

data class UsuarioEditRQ(
    val id: Long?,
    val nome: String,
    val sobrenome: String,
    val email: String,
    val telContato: String,
    val telContatoZap: String,
)