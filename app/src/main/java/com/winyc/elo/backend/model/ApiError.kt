package com.winyc.elo.backend.model

data class ApiError(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)