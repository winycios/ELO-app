package com.winyc.elo.backend.model

import com.winyc.elo.backend.model.enums.CadastroAcao

data class UsuarioRQ(

    val nome: String,
    val sobrenome: String,
    val email: String,
    val telContato: String,
    val senha: String,
    val isDuplicarTel: Boolean,
    val cadastroAcao: CadastroAcao
)