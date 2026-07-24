package com.winyc.elo.backend.controller.profissional

import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.profissional.ProfissionalRS
import com.winyc.elo.backend.model.profissional.ProfissionalUpdateRQ
import com.winyc.elo.backend.model.servico.ServicoCreateRQ
import com.winyc.elo.backend.model.servico.ServicoListaRS
import com.winyc.elo.backend.model.servico.ServicoRS
import com.winyc.elo.backend.model.vitrine.PublicacaoCreateRQ
import com.winyc.elo.backend.model.vitrine.PublicacaoFeedRS
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfissionalInterface {

    companion object {
        private const val PATH = "profissional/"
    }

    @GET(PATH + "perfil")
    fun buscarPerfil(): Call<ProfissionalRS>

    @PUT(PATH + "perfil")
    fun salvarPerfil(@Body dto: ProfissionalUpdateRQ): Call<ProfissionalRS>

    @PATCH(PATH + "disponivel/{isAtivar}")
    fun habilitarDisponibilidade(@Path("isAtivar") isAtivar: Boolean): Call<Void>

    @POST(PATH + "servico")
    fun salvarServico(@Body dto: ServicoCreateRQ): Call<ServicoRS>

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
    fun salvarPublicacao(@Body dto: PublicacaoCreateRQ): Call<PublicacaoFeedRS>

    @DELETE("vitrine/" + PATH + "publicacao/{id}")
    fun desativarPublicacao(@Path("id") id: Long): Call<Void>
}
