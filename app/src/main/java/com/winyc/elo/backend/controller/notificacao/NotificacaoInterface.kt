package com.winyc.elo.backend.controller.notificacao

import com.winyc.elo.backend.model.notificacao.DispositivoRQ
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificacaoInterface {

    companion object {
        private const val PATH = "notificacoes/"
    }

    @PUT(PATH + "dispositivos")
    fun registrarDispositivo(@Body dto: DispositivoRQ): Call<Void>

    @DELETE(PATH + "dispositivos/{codigoDispositivo}")
    fun desativarDispositivo(@Path("codigoDispositivo") codigoDispositivo: String): Call<Void>
}
