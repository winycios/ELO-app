package com.winyc.elo.backend.controller.profissional

import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.servico.ServicoCreateDTO
import com.winyc.elo.backend.model.servico.ServicoListaRS
import com.winyc.elo.backend.model.servico.ServicoRS
import com.winyc.elo.backend.model.vitrine.PublicacaoCreateDTO
import com.winyc.elo.backend.model.vitrine.PublicacaoFeedRS
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("vitrine/" + PATH + "listar")
    fun listarMinhasPublicacoes(
        @Query("categoriaId") categoriaId: Long?,
        @Query("cursor") cursor: String?,
    ): Call<CursorPageRS<PublicacaoFeedRS>>

    @POST("vitrine/" + PATH + "publicacao")
    fun salvarPublicacao(@Body dto: PublicacaoCreateDTO): Call<PublicacaoFeedRS>

    @DELETE("vitrine/" + PATH + "publicacao/{id}")
    fun desativarPublicacao(@Path("id") id: Long): Call<Void>
}
