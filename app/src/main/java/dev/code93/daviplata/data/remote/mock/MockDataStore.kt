package dev.code93.daviplata.data.remote.mock

import dev.code93.daviplata.BuildConfig
import dev.code93.daviplata.security.PasswordHasher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockDataStore @Inject constructor(private val hasher: PasswordHasher) {

    data class MockUser(
        val userId: String,
        val name: String,
        val phone: String,
        val passwordHash: String,
        val email: String,
        val document: String,
        var balance: Double,
        val username: String,
    )

    data class MockSession(
        val sessionId: String,
        val userId: String,
        val expiresAt: Long,
    )

    data class MockMovement(
        val id: String,
        val userId: String,
        val type: String,
        val status: String,
        val amount: Double,
        val description: String,
        val occurredAt: Long,
    )

    private val mutex = Mutex()
    private val users = mutableListOf<MockUser>()
    private val sessions = mutableListOf<MockSession>()
    private val movements = mutableListOf<MockMovement>()
    private val failedAttempts = mutableMapOf<String, Int>()
    private val lockUntil = mutableMapOf<String, Long>()

    init {
        seed()
    }

    private fun seed() {
        val now = System.currentTimeMillis()
        val felipe = MockUser(
            userId = "user-001",
            name = "Fabian Ardila",
            phone = "3001234567",
            passwordHash = hasher.hash("demo1234"),
            email = "fabiane@demo.co",
            document = "1234567890",
            balance = 1_250_345.0,
            username = "fabian.ardila",
        )
        val ana = MockUser(
            userId = "user-002",
            name = "Ana Gómez",
            phone = "3009876543",
            passwordHash = hasher.hash("demo1234"),
            email = "ana@demo.co",
            document = "9876543210",
            balance = 480_000.0,
            username = "ana.gomez",
        )
        users.addAll(listOf(felipe, ana))

        val day = 86_400_000L
        // Mezcla intencional de status para que la UI muestre los 3 caminos (badge "Pendiente", strikethrough "Fallido", normal).
        movements.addAll(listOf(
            MockMovement("mov-01", felipe.userId, "CREDIT", "COMPLETED", 150_000.0, "Recibido de Camila Ruiz",  now - day * 1),
            MockMovement("mov-02", felipe.userId, "DEBIT",  "COMPLETED",  50_000.0, "Pago Claro móvil",         now - day * 2),
            MockMovement("mov-03", felipe.userId, "DEBIT",  "PENDING",    20_000.0, "Recarga Viva Air",         now - day * 2),
            MockMovement("mov-04", felipe.userId, "CREDIT", "COMPLETED", 500_000.0, "Nómina mayo",              now - day * 4),
            MockMovement("mov-05", felipe.userId, "DEBIT",  "COMPLETED",  80_000.0, "Supermercado Jumbo",       now - day * 5),
            MockMovement("mov-06", felipe.userId, "DEBIT",  "COMPLETED",  12_500.0, "Netflix",                  now - day * 7),
            MockMovement("mov-07", felipe.userId, "CREDIT", "COMPLETED", 200_000.0, "Recibido de Andrés P.",    now - day * 8),
            MockMovement("mov-08", felipe.userId, "DEBIT",  "FAILED",     35_000.0, "Rappi domicilio",          now - day * 10),
            MockMovement("mov-09", felipe.userId, "DEBIT",  "COMPLETED",  45_000.0, "ETB internet",             now - day * 12),
            MockMovement("mov-10", felipe.userId, "CREDIT", "COMPLETED",  75_000.0, "Devolución Falabella",     now - day * 14),
            MockMovement("mov-11", felipe.userId, "DEBIT",  "COMPLETED", 100_000.0, "Pago arriendo parcial",    now - day * 18),
            MockMovement("mov-12", felipe.userId, "DEBIT",  "COMPLETED",   8_000.0, "Parkimóvil",               now - day * 20),
        ))
    }

    suspend fun findUserByPhone(phone: String): MockUser? = mutex.withLock {
        users.find { it.phone == phone }
    }

    suspend fun findUserByUserId(userId: String): MockUser? = mutex.withLock {
        users.find { it.userId == userId }
    }

    suspend fun authenticate(phone: String, password: String, hasher: PasswordHasher): MockUser? = mutex.withLock {
        val user = users.find { it.phone == phone } ?: return@withLock null
        if (!hasher.verify(password, user.passwordHash)) return@withLock null
        failedAttempts.remove(phone)
        user
    }

    suspend fun recordFailedAttempt(phone: String): Int = mutex.withLock {
        val count = (failedAttempts[phone] ?: 0) + 1
        failedAttempts[phone] = count
        if (count >= 3) lockUntil[phone] = System.currentTimeMillis() + 5 * 60_000L
        count
    }

    suspend fun isLocked(phone: String): Long? = mutex.withLock {
        val until = lockUntil[phone] ?: return@withLock null
        if (System.currentTimeMillis() < until) until else { lockUntil.remove(phone); failedAttempts.remove(phone); null }
    }

    suspend fun createUser(phone: String, name: String, document: String, email: String, username: String, passwordHash: String): MockUser = mutex.withLock {
        val user = MockUser(
            userId = UUID.randomUUID().toString(),
            name = name, phone = phone, passwordHash = passwordHash,
            email = email, document = document, balance = 0.0, username = username,
        )
        users.add(user)
        user
    }

    suspend fun createSession(userId: String): MockSession = mutex.withLock {
        val session = MockSession(
            sessionId = UUID.randomUUID().toString(),
            userId = userId,
            expiresAt = System.currentTimeMillis() + BuildConfig.SESSION_TTL_MINUTES * 60_000L,
        )
        sessions.add(session)
        session
    }

    suspend fun findSession(sessionId: String): MockSession? = mutex.withLock {
        sessions.find { it.sessionId == sessionId }
    }

    suspend fun expireSession(sessionId: String) = mutex.withLock {
        sessions.removeAll { it.sessionId == sessionId }
    }

    suspend fun getMovementsPage(userId: String, page: Int, size: Int): Pair<List<MockMovement>, Int> = mutex.withLock {
        val all = movements.filter { it.userId == userId }.sortedByDescending { it.occurredAt }
        val total = all.size
        val items = all.drop(page * size).take(size)
        Pair(items, total)
    }

    suspend fun createTransfer(fromUserId: String, toPhone: String, amount: Double, description: String): MockMovement = mutex.withLock {
        val sender = users.find { it.userId == fromUserId }
            ?: throw MockException(400, "USER_NOT_FOUND", "Usuario no encontrado")
        if (amount > sender.balance) throw MockException(409, "INSUFFICIENT_FUNDS", "Saldo insuficiente")
        sender.balance -= amount

        val debit = MockMovement(
            id = UUID.randomUUID().toString(),
            userId = fromUserId, type = "DEBIT", status = "COMPLETED", amount = amount,
            description = if (description.isBlank()) "Transferencia a $toPhone" else description,
            occurredAt = System.currentTimeMillis(),
        )
        movements.add(debit)

        val recipient = users.find { it.phone == toPhone }
        recipient?.let {
            it.balance += amount
            movements.add(
                MockMovement(
                    id = UUID.randomUUID().toString(),
                    userId = it.userId, type = "CREDIT", status = "COMPLETED", amount = amount,
                    description = "Recibido de ${sender.name}",
                    occurredAt = System.currentTimeMillis(),
                )
            )
        }
        debit
    }
}

class MockException(val code: Int, val errorCode: String, msg: String) : Exception(msg)
