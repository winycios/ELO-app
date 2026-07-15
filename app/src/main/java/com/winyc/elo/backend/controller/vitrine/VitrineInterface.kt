package com.winyc.elo.backend.controller.vitrine

import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.vitrine.ComentarioCreateRQ
import com.winyc.elo.backend.model.vitrine.ComentarioRS
import com.winyc.elo.backend.model.vitrine.PublicacaoFeedRS
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VitrineInterface {

    companion object {
        private const val PATH = "vitrine/"
    }

    @GET(PATH + "listar")
    fun listarFeed(
        @Query("categoriaId") categoriaId: Long?,
        @Query("cursor") cursor: String?,
    ): Call<CursorPageRS<PublicacaoFeedRS>>

    @POST(PATH + "publicacoes/{publicacaoId}/curtidas")
    fun curtir(@Path("publicacaoId") publicacaoId: Long): Call<Void>

    @DELETE(PATH + "publicacoes/{publicacaoId}/curtidas")
    fun descurtir(@Path("publicacaoId") publicacaoId: Long): Call<Void>

    @POST(PATH + "publicacoes/{publicacaoId}/comentarios")
    fun comentar(
        @Path("publicacaoId") publicacaoId: Long,
        @Body request: ComentarioCreateRQ,
    ): Call<ComentarioRS>

    @GET(PATH + "publicacoes/{publicacaoId}/comentarios")
    fun listarComentarios(
        @Path("publicacaoId") publicacaoId: Long,
        @Query("cursor") cursor: String?,
    ): Call<CursorPageRS<ComentarioRS>>
}
