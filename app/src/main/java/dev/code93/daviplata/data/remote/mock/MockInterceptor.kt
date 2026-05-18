package dev.code93.daviplata.data.remote.mock

import com.squareup.moshi.Moshi
import dev.code93.daviplata.BuildConfig
import dev.code93.daviplata.security.PasswordHasher
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockInterceptor @Inject constructor(
    private val store: MockDataStore,
    private val moshi: Moshi,
    private val hasher: PasswordHasher,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        Thread.sleep(BuildConfig.MOCK_DELAY_MS)

        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method

        return try {
            if (request.header("X-Mock-Force") == "500") {
                return errorResponse(request, 500, "INTERNAL_ERROR", "Error interno del servidor")
            }

            runBlocking {
                when {
                    method == "POST" && path.endsWith("/api/auth/register") -> handleRegister(request)
                    method == "POST" && path.endsWith("/api/auth/login")    -> handleLogin(request)
                    method == "POST" && path.endsWith("/api/auth/logout")   -> handleLogout(request)
                    method == "GET"  && path.endsWith("/api/accounts/balance") -> handleBalance(request)
                    method == "GET"  && path.endsWith("/api/movements")     -> handleMovements(request)
                    method == "POST" && path.endsWith("/api/transfers")     -> handleTransfer(request)
                    method == "GET"  && path.endsWith("/api/session/validate") -> handleValidate(request)
                    method == "GET"  && path.contains("/api/users/")        -> handleFindUser(request)
                    else -> errorResponse(request, 404, "NOT_FOUND", "Ruta no encontrada: $path")
                }
            }
        } catch (e: MockException) {
            errorResponse(request, e.code, e.errorCode, e.message ?: "Error")
        }
    }

    private suspend fun handleRegister(request: okhttp3.Request): Response {
        val body = request.bodyAsString()
        val map = moshi.adapter(Map::class.java).fromJson(body) ?: emptyMap<String, Any>()
        val phone    = map["phone"] as? String ?: ""
        val name     = map["name"] as? String ?: ""
        val document = map["document"] as? String ?: ""
        val email    = map["email"] as? String ?: ""
        val username = map["username"] as? String ?: ""
        val password = map["password"] as? String ?: ""

        if (store.findUserByPhone(phone) != null)
            return errorResponse(request, 409, "PHONE_TAKEN", "Este número ya tiene una cuenta")

        val user = store.createUser(phone, name, document, email, username, hasher.hash(password))
        return okResponse(request, 200, """{"userId":"${user.userId}","name":"${user.name}","phone":"${user.phone}"}""")
    }

    private suspend fun handleLogin(request: okhttp3.Request): Response {
        val body = request.bodyAsString()
        val map = moshi.adapter(Map::class.java).fromJson(body) ?: emptyMap<String, Any>()
        val phone    = map["phone"] as? String ?: ""
        val password = map["password"] as? String ?: ""

        store.isLocked(phone)?.let { until ->
            val remaining = ((until - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            return errorResponse(request, 423, "ACCOUNT_LOCKED", "Cuenta bloqueada. Reintenta en ${remaining}s", """{"retryAfterSeconds":$remaining}""")
        }

        val user = store.authenticate(phone, password, hasher)
        if (user == null) {
            val attempts = store.recordFailedAttempt(phone)
            return errorResponse(request, 401, "INVALID_CREDENTIALS", "Usuario o contraseña incorrectos. Intento $attempts de 3.")
        }

        val session = store.createSession(user.userId)
        return okResponse(request, 200, """{"sessionId":"${session.sessionId}","userId":"${user.userId}","name":"${user.name}","phone":"${user.phone}","expiresAt":${session.expiresAt}}""")
    }

    private suspend fun handleLogout(request: okhttp3.Request): Response {
        val sessionId = extractSessionId(request)
        if (sessionId != null) store.expireSession(sessionId)
        return okResponse(request, 204, "")
    }

    private suspend fun handleBalance(request: okhttp3.Request): Response {
        val session = requireValidSession(request)
        val user = store.findUserByUserId(session.userId)
            ?: return errorResponse(request, 404, "USER_NOT_FOUND", "Usuario no encontrado")
        return okResponse(request, 200, """{"userId":"${user.userId}","balance":${user.balance},"currency":"COP"}""")
    }

    private suspend fun handleMovements(request: okhttp3.Request): Response {
        val session = requireValidSession(request)
        val page = request.url.queryParameter("page")?.toIntOrNull() ?: 0
        val size = request.url.queryParameter("size")?.toIntOrNull() ?: 20
        val (items, total) = store.getMovementsPage(session.userId, page, size)
        val itemsJson = items.joinToString(",") { m ->
            """{"id":"${m.id}","type":"${m.type}","status":"${m.status}","amount":${m.amount},"description":"${m.description.replace("\"", "\\\"")}","occurredAt":${m.occurredAt}}"""
        }
        return okResponse(request, 200, """{"page":$page,"size":$size,"total":$total,"items":[$itemsJson]}""")
    }

    private suspend fun handleTransfer(request: okhttp3.Request): Response {
        val session = requireValidSession(request)
        val body = request.bodyAsString()
        val map = moshi.adapter(Map::class.java).fromJson(body) ?: emptyMap<String, Any>()
        val toPhone     = map["toPhone"] as? String ?: ""
        val amount      = (map["amount"] as? Number)?.toDouble() ?: 0.0
        val description = map["description"] as? String ?: ""

        if (amount <= 0)
            return errorResponse(request, 400, "INVALID_AMOUNT", "El monto debe ser mayor a 0")
        if (toPhone == "0000000000")
            return errorResponse(request, 404, "RECIPIENT_NOT_FOUND", "El número de destino no está registrado en DaviPlata")

        val recipient = store.findUserByPhone(toPhone)
            ?: return errorResponse(request, 404, "RECIPIENT_NOT_FOUND", "El número de destino no está registrado en DaviPlata")

        val movement = store.createTransfer(session.userId, toPhone, amount, description)
        val sender = store.findUserByUserId(session.userId)!!
        return okResponse(request, 200, """{"transferId":"${movement.id}","status":"COMPLETED","newBalance":${sender.balance},"createdAt":${movement.occurredAt},"recipientName":"${recipient.name}"}""")
    }

    private suspend fun handleValidate(request: okhttp3.Request): Response {
        val sessionId = extractSessionId(request)
            ?: return okResponse(request, 401, """{"valid":false,"reason":"INVALID"}""")
        val session = store.findSession(sessionId)
            ?: return okResponse(request, 401, """{"valid":false,"reason":"INVALID"}""")
        if (System.currentTimeMillis() >= session.expiresAt)
            return okResponse(request, 401, """{"valid":false,"reason":"EXPIRED"}""")
        val remaining = (session.expiresAt - System.currentTimeMillis()) / 1000
        return okResponse(request, 200, """{"valid":true,"expiresAt":${session.expiresAt},"remainingSeconds":$remaining}""")
    }

    private suspend fun handleFindUser(request: okhttp3.Request): Response {
        requireValidSession(request)
        val phone = request.url.pathSegments.lastOrNull() ?: ""
        val user = store.findUserByPhone(phone)
            ?: return errorResponse(request, 404, "USER_NOT_FOUND", "Usuario no encontrado")
        return okResponse(request, 200, """{"userId":"${user.userId}","name":"${user.name}","phone":"${user.phone}"}""")
    }

    private suspend fun requireValidSession(request: okhttp3.Request): MockDataStore.MockSession {
        val sessionId = extractSessionId(request)
            ?: throw MockException(401, "SESSION_EXPIRED", "Sesión inválida")
        val session = store.findSession(sessionId)
            ?: throw MockException(401, "SESSION_EXPIRED", "Sesión no encontrada")
        if (System.currentTimeMillis() >= session.expiresAt)
            throw MockException(401, "SESSION_EXPIRED", "La sesión ha expirado")
        return session
    }

    private fun extractSessionId(request: okhttp3.Request): String? {
        val auth = request.header("Authorization") ?: return null
        return if (auth.startsWith("Bearer ")) auth.removePrefix("Bearer ").trim() else null
    }

    private fun okhttp3.Request.bodyAsString(): String {
        val buffer = okio.Buffer()
        body?.writeTo(buffer)
        return buffer.readUtf8()
    }

    private fun okResponse(request: okhttp3.Request, code: Int, json: String) = Response.Builder()
        .request(request).code(code).protocol(Protocol.HTTP_1_1)
        .message(if (code == 204) "No Content" else "OK")
        .body(json.toResponseBody("application/json".toMediaType()))
        .build()

    private fun errorResponse(request: okhttp3.Request, code: Int, errorCode: String, message: String, extra: String = ""): Response {
        val body = """{"code":"$errorCode","message":"${message.replace("\"", "\\\"")}"${ if (extra.isNotEmpty()) ",$extra" else ""}}"""
        return Response.Builder()
            .request(request).code(code).protocol(Protocol.HTTP_1_1).message("Error")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }
}
