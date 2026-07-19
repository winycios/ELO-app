package com.winyc.elo.backend.controller.busca

import com.winyc.elo.backend.model.search.BuscaProfissionalRS
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BuscaInterface {

    companion object {
        private const val PATH = "busca/"
    }

    /**
     * Busca profissionais. Parâmetros nulos são omitidos pelo Retrofit — é assim
     * que o cliente deslogado (sem [latitude]/[longitude]) evita enviar a
     * localização, enquanto o logado manda o máximo de informação.
     */
    @GET(PATH + "profissionais")
    fun buscarProfissionais(
        @Query("texto") texto: String?,
        @Query("categoriaId") categoriaId: Long?,
        @Query("avaliacaoMinima") avaliacaoMinima: Double?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("distanciaKm") distanciaKm: Double?,
        @Query("ordenacao") ordenacao: String?,
        @Query("pagina") pagina: Int?,
        @Query("tamanho") tamanho: Int?,
    ): Call<BuscaProfissionalRS>
}
