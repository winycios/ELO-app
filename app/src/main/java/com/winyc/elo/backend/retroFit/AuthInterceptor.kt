package com.winyc.elo.backend.retroFit

import com.winyc.elo.backend.security.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Anexa o header `Authorization: Bearer <accessToken>` em toda requisição
 * que sai pelo cliente autenticado. Se ainda não houver token (usuário
 * deslogado), a requisição segue sem o header.
 */
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requisicaoOriginal = chain.request()
        val token = tokenStore.accessToken

        val requisicao = if (token.isNullOrBlank()) {
            requisicaoOriginal
        } else {
            requisicaoOriginal.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        return chain.proceed(requisicao)
    }
}