package com.winyc.elo.backend.controller.orcamento

import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.orcamento.HorariosDisponiveisRS
import com.winyc.elo.backend.model.orcamento.OrcamentoCreateRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheRS
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemRS
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

    @GET("$PATH/listar")
    fun listarOrcamentos(
        @Query("status") status: String?,
        @Query("cursor") cursor: String?,
        @Query("tamanho") tamanho: Int,
    ): Call<CursorPageRS<OrcamentoListagemRS>>

    @GET("$PATH/{orcamentoId}")
    fun buscarOrcamentoPorId(@Path("orcamentoId") orcamentoId: Long): Call<OrcamentoDetalheRS>
}
