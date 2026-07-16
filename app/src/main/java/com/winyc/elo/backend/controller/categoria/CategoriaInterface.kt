package com.winyc.elo.backend.controller.categoria

import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.categoria.CategoriaRS
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

interface CategoriaInterface {

    companion object {
        private const val PATH = "categoria/"
    }

    @GET(PATH + "listar")
    fun listarCategoria(
    ): Call<List<CategoriaRS>>
}
