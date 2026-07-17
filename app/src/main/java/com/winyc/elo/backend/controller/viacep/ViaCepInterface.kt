package com.winyc.elo.backend.controller.viacep

import com.winyc.elo.backend.model.endereco.ViaCepRS
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ViaCepInterface {

    @GET("{cep}/json/")
    fun consultar(@Path("cep") cep: String): Call<ViaCepRS>
}
