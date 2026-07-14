package com.winyc.elo.backend.model

data class AuthRS(
    val id: Long,
    val token: String,
    val refreshToken: String,
    val nome: String,
    val urlPerfil: String?,
    val urlPerfilPro: String?,
    val isProfissionalAtivo: Boolean,
    val isClienteAtivo: Boolean
)

data class AuthRQ(
    val email: String,
    val senha: String,
    val deviceCode: String
)