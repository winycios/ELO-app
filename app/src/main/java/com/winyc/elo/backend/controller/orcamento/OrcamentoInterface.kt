package com.winyc.elo.backend.controller.orcamento

import com.winyc.elo.backend.model.CursorPageRS
import com.winyc.elo.backend.model.orcamento.HorariosDisponiveisRS
import com.winyc.elo.backend.model.orcamento.AvaliacaoOrcamentoRQ
import com.winyc.elo.backend.model.orcamento.AvaliacaoOrcamentoRS
import com.winyc.elo.backend.model.orcamento.OrcamentoCancelamentoRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoConclusaoRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoCreateRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheProfissionalRS
import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheRS
import com.winyc.elo.backend.model.orcamento.OrcamentoFinalCreateRQ
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemProfissionalRS
import com.winyc.elo.backend.model.orcamento.OrcamentoListagemRS
import com.winyc.elo.backend.model.orcamento.OrcamentoRS
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
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

    @PATCH("$PATH/usuario/{orcamentoId}/aprovar")
    fun aprovarOrcamentoFinal(@Path("orcamentoId") orcamentoId: Long): Call<OrcamentoDetalheRS>

    @PATCH("$PATH/usuario/{orcamentoId}/cancelar")
    fun cancelarOrcamentoCliente(
        @Path("orcamentoId") orcamentoId: Long,
        @Body dto: OrcamentoCancelamentoRQ,
    ): Call<OrcamentoDetalheRS>

    @GET("$PATH/profissional/listar")
    fun listarOrcamentosProfissional(
        @Query("status") status: String?,
        @Query("cursor") cursor: String?,
        @Query("tamanho") tamanho: Int,
    ): Call<CursorPageRS<OrcamentoListagemProfissionalRS>>

    @GET("$PATH/profissional/{orcamentoId}")
    fun buscarOrcamentoPorIdProfissional(
        @Path("orcamentoId") orcamentoId: Long,
    ): Call<OrcamentoDetalheProfissionalRS>

    @POST("$PATH/profissional/{orcamentoId}/final")
    fun enviarOrcamentoFinal(
        @Path("orcamentoId") orcamentoId: Long,
        @Body dto: OrcamentoFinalCreateRQ,
    ): Call<OrcamentoDetalheProfissionalRS>

    @PATCH("$PATH/profissional/{orcamentoId}/recusar")
    fun cancelarOrcamentoProfissional(
        @Path("orcamentoId") orcamentoId: Long,
        @Body dto: OrcamentoCancelamentoRQ,
    ): Call<OrcamentoDetalheProfissionalRS>

    @POST("$PATH/usuario/{orcamentoId}/avaliar")
    fun avaliarProfissional(
        @Path("orcamentoId") orcamentoId: Long,
        @Body dto: AvaliacaoOrcamentoRQ,
    ): Call<AvaliacaoOrcamentoRS>

    @PATCH("$PATH/profissional/{orcamentoId}/concluir")
    fun concluirOrcamento(
        @Path("orcamentoId") orcamentoId: Long,
        @Body dto: OrcamentoConclusaoRQ,
    ): Call<OrcamentoDetalheProfissionalRS>

    @POST("$PATH/profissional/{orcamentoId}/avaliar")
    fun avaliarCliente(
        @Path("orcamentoId") orcamentoId: Long,
        @Body dto: AvaliacaoOrcamentoRQ,
    ): Call<AvaliacaoOrcamentoRS>
}
