package com.winyc.elo.backend.model

data class CursorPageRS<T>(
    val items: List<T>,
    val nextCursor: String?,
    val hasNext: Boolean,
)
