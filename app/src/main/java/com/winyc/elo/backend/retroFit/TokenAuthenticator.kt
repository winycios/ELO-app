package com.winyc.elo.backend.retroFit

import com.winyc.elo.backend.controller.auth.AuthInterface
import com.winyc.elo.backend.model.RefreshTokenRQ
import com.winyc.elo.backend.security.TokenStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val authApi: AuthInterface,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (contarTentativas(response) >= 2) {
            tokenStore.limparBloqueante()
            return null
        }

        val refresh = tokenStore.refreshToken ?: return null

        synchronized(this) {
            val tokenAtual = tokenStore.accessToken
            val tokenDaRequisicao = response.request.header("Authorization")

            if (!tokenAtual.isNullOrBlank() && tokenDaRequisicao != "Bearer $tokenAtual") {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $tokenAtual")
                    .build()
            }

            val nova = try {
                authApi.recarregarToken(RefreshTokenRQ(refresh)).execute()
            } catch (_: Exception) {
                null
            }

            val corpo = nova?.body()
            if (nova == null || !nova.isSuccessful || corpo == null) {
                tokenStore.limparBloqueante()
                return null
            }

            tokenStore.salvarBloqueante(corpo)
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${corpo.token}")
                .build()
        }
    }

    /** Conta quantas respostas (401) já ocorreram nesta cadeia de tentativas. */
    private fun contarTentativas(response: Response): Int {
        var anterior = response.priorResponse
        var total = 1
        while (anterior != null) {
            total++
            anterior = anterior.priorResponse
        }
        return total
    }
}