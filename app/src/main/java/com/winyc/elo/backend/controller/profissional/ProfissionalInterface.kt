package com.winyc.elo.backend.controller.profissional

import com.winyc.elo.backend.model.servico.ServicoCreateDTO
import com.winyc.elo.backend.model.servico.ServicoListaRS
import com.winyc.elo.backend.model.servico.ServicoRS
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProfissionalInterface {

    companion object {
        private const val PATH = "profissional/"
    }

    @POST(PATH + "servico")
    fun salvarServico(@Body dto: ServicoCreateDTO): Call<ServicoRS>

    @GET(PATH + "servico/listar")
    fun listarServicos(): Call<List<ServicoListaRS>>

    @GET(PATH + "servico/{id}")
    fun buscarServico(@Path("id") id: Long): Call<ServicoRS>

    @DELETE(PATH + "servico/{id}")
    fun desativarServico(@Path("id") id: Long): Call<Void>
}
