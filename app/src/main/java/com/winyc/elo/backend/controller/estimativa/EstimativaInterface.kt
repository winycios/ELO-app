package com.winyc.elo.backend.controller.estimativa

import com.winyc.elo.backend.model.estimativa.ProfissionalServicoRS
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EstimativaInterface {

    companion object {
        private const val PATH = "estimativa/"
    }

    /** Detalhes a partir de um serviço específico (clique vindo da busca). */
    @GET(PATH + "profissional/{id}/detalhes")
    fun buscarPorServico(
        @Path("id") profissionalId: Long,
        @Query("servicoId") servicoId: Long,
    ): Call<ProfissionalServicoRS>

    /** Detalhes a partir de uma categoria (clique vindo da vitrine). */
    @GET(PATH + "profissional/{id}/detalhes")
    fun buscarPorCategoria(
        @Path("id") profissionalId: Long,
        @Query("categoriaId") categoriaId: Long,
    ): Call<ProfissionalServicoRS>
}
