package com.winyc.elo.backend.controller.auth

import com.winyc.elo.backend.model.AuthRQ
import com.winyc.elo.backend.model.AuthRS
import com.winyc.elo.backend.model.UsuarioRQ
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthInterface {

    companion object {
        private const val PATH = "auth/"
    }

    @POST(PATH + "refresh")
    @Headers(
        "Accept: */*",
        "Connection: keep-alive",
        "Cache-Control: no-cache",
        "Content-Type: application/x-www-form-urlencoded"
    )
    fun recarregarToken(@Body refreshToken: String): Call<AuthRS>

    @POST(PATH + "login")
    @Headers(
        "Content-Type: application/json",
        "Accept: */*",
        "Connection: keep-alive",
        "Cache-Control: no-cache"
    )
    fun logar(@Body auth: AuthRQ): Call<AuthRS>

    @POST(PATH + "create")
    @Headers(
        "Content-Type: application/json",
        "Accept: */*",
        "Connection: keep-alive",
        "Cache-Control: no-cache"
    )
    fun criarUsuario(@Body usuarioRQ: UsuarioRQ): Call<Void>

}