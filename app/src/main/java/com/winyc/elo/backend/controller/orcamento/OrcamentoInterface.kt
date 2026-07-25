package com.winyc.elo.backend.controller.orcamento

import com.winyc.elo.backend.model.orcamento.HorariosDisponiveisRS
import com.winyc.elo.backend.model.orcamento.OrcamentoCreateRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoRS
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrcamentoInterface {

    companion object {
        private const val PATH = "orcamento"
    }


    @GET("$PATH/servico/{servicoId}/horarios-disponiveis")
    fun buscarHorariosDisponiveis(
        @Path("servicoId") servicoId: Long,
        @Query("dataReferencia") dataReferencia: String?,
    ): Call<HorariosDisponiveisRS>

    @POST(PATH)
    fun solicitarOrcamento(@Body dto: OrcamentoCreateRQ): Call<OrcamentoRS>
}
