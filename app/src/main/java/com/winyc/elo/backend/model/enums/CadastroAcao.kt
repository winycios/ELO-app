package com.winyc.elo.backend.model.enums

enum class CadastroAcao(val tpAcao: Int) {

    CADASTRAR_USUARIO(1),
    CADASTRAR_PROFISSIONAL(2),
    CADASTRAR_USUPRO(3);

    fun isCadastrarUsuario(): Boolean =
        this == CADASTRAR_USUARIO

    fun isCadastrarProfissional(): Boolean =
        this == CADASTRAR_PROFISSIONAL

    fun isCadastrarUsuPro(): Boolean = this == CADASTRAR_USUPRO

    companion object {
        fun fromTpAcao(tpAcao: Int): CadastroAcao =
            entries.firstOrNull { it.tpAcao == tpAcao }
                ?: throw IllegalArgumentException("Tipo de ação inválido: $tpAcao")
    }
}