package dev.code93.daviplata.data.remote

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.code93.daviplata.bridge.BridgeEventBus
import dev.code93.daviplata.bridge.BridgeEvents
import dev.code93.daviplata.data.local.SecureStorage
import dev.code93.daviplata.presentation.session.SessionExpiredActivity
import dev.code93.daviplata.security.SessionGuard
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val storage: SecureStorage,
    private val sessionGuard: SessionGuard,
    private val eventBus: BridgeEventBus,
    @param:ApplicationContext private val context: Context,
) : Interceptor {

    private companion object {
        // Endpoints donde un 401 NO significa "sesión expirada" sino "credenciales inválidas",
        // y donde tampoco se debe pre-validar la sesión (login/register no requieren una previa).
        val AUTH_ENDPOINTS = listOf("/api/auth/login", "/api/auth/register")

        // Cuerpo JSON con el mismo shape que produce MockInterceptor para SESSION_EXPIRED,
        // de modo que ErrorMapper lo deserialice a AppError.SessionExpired aguas abajo.
        const val SESSION_EXPIRED_BODY = """{"code":"SESSION_EXPIRED","message":"La sesión ha expirado"}"""
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val isAuthEndpoint = AUTH_ENDPOINTS.any { path.endsWith(it) }

        // Pre-validación: si la sesión ya está vencida (o no existe) y el endpoint requiere
        // autenticación, cortocircuita con un 401 sintético — evita el round-trip al backend
        // y dispara el flujo de SessionExpiredActivity inmediatamente.
        if (!isAuthEndpoint && sessionGuard.isExpired()) {
            handleSessionExpired()
            return syntheticUnauthorized(request)
        }

        val sessionId = storage.getSession()?.sessionId
        val authedRequest = if (sessionId != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $sessionId")
                .build()
        } else {
            request
        }

        val response = chain.proceed(authedRequest)
        if (response.code == 401 && !isAuthEndpoint) {
            handleSessionExpired()
        }
        return response
    }

    private fun handleSessionExpired() {
        storage.clear()
        eventBus.emit(BridgeEvents.SESSION_EXPIRED)
        launchSessionExpired()
    }

    private fun syntheticUnauthorized(request: okhttp3.Request): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body(SESSION_EXPIRED_BODY.toResponseBody("application/json".toMediaType()))
            .build()

    private fun launchSessionExpired() {
        context.startActivity(
            Intent(context, SessionExpiredActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }
}
