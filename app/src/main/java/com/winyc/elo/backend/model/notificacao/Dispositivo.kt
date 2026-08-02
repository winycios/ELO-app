package com.winyc.elo.backend.model.notificacao

import com.winyc.elo.backend.model.enums.Plataforma
import com.winyc.elo.backend.model.enums.TipoIdentificador

data class DispositivoRQ(
    val codigoDispositivo: String,
    val identificadorFcm: String,
    val tipoIdentificador: TipoIdentificador = TipoIdentificador.FID,
    val plataforma: Plataforma = Plataforma.ANDROID,
)
