package com.winyc.elo.backend.controller.categoria

import com.winyc.elo.backend.model.categoria.CategoriaRS
import retrofit2.Call
import retrofit2.http.GET

interface CategoriaInterface {

    companion object {
        private const val PATH = "categoria/"
    }

    @GET(PATH + "listar")
    fun listarCategoria(
    ): Call<List<CategoriaRS>>
}
