package dev.code93.daviplata.security

import dev.code93.daviplata.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

class SessionExpiredException : Exception("La sesión ha expirado")

@Singleton
class SessionGuard @Inject constructor(
    private val sessionRepo: SessionRepository,
) {
    fun ensureValid() {
        val session = sessionRepo.current()
            ?: throw SessionExpiredException()
        if (System.currentTimeMillis() >= session.expiresAtMillis) {
            throw SessionExpiredException()
        }
    }

    fun isExpired(): Boolean {
        val session = sessionRepo.current() ?: return true
        return System.currentTimeMillis() >= session.expiresAtMillis
    }
}
