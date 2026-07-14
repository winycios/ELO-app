package com.winyc.elo.backend.model

import com.google.crypto.tink.shaded.protobuf.Timestamp

data class ApiError(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)