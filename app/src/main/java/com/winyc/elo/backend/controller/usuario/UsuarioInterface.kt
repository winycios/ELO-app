package com.winyc.elo.backend.controller.usuario

import com.winyc.elo.backend.model.endereco.EnderecoCreateRQ
import com.winyc.elo.backend.model.endereco.EnderecoRS
import com.winyc.elo.backend.model.usuario.UsuarioEditRQ
import com.winyc.elo.backend.model.usuario.UsuarioRS
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioInterface {

    companion object {
        private const val PATH = "usuario/"
    }

    @GET(PATH + "perfil")
    fun pegarPerfil(): Call<UsuarioRS>

    @PUT(PATH + "perfil")
    fun editarPerfil(@Body dto: UsuarioEditRQ): Call<UsuarioRS>

    @POST(PATH + "endereco")
    fun salvarEndereco(@Body dto: EnderecoCreateRQ): Call<EnderecoRS>

    @GET(PATH + "endereco/principal")
    fun buscarPrincipal(): Call<EnderecoRS>

    @GET(PATH + "endereco/listar")
    fun listarEnderecos(): Call<List<EnderecoRS>>

    @PATCH(PATH + "endereco/principal/{id}")
    fun definirPrincipal(@Path("id") id: Long): Call<Void>

    @DELETE(PATH + "endereco/principal/{id}")
    fun desativarEndereco(@Path("id") id: Long): Call<Void>
}
