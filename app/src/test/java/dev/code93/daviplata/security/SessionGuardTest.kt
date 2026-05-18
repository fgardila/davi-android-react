package dev.code93.daviplata.security

import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.repository.SessionRepository
import dev.code93.daviplata.security.SessionExpiredException
import dev.code93.daviplata.security.SessionGuard
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionGuardTest {

    private val sessionRepo: SessionRepository = mockk(relaxed = true)
    private val guard = SessionGuard(sessionRepo)

    private fun session(expiresAtMillis: Long) = Session(
        sessionId = "sid",
        userId = "u1",
        name = "Felipe",
        phone = "3001234567",
        expiresAtMillis = expiresAtMillis,
    )

    @Test
    fun `ensureValid throws when no session`() {
        every { sessionRepo.current() } returns null
        try {
            guard.ensureValid()
            error("Expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun `ensureValid throws when session is expired`() {
        every { sessionRepo.current() } returns session(System.currentTimeMillis() - 1000L)
        try {
            guard.ensureValid()
            error("Expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun `ensureValid does not throw when session is valid`() {
        every { sessionRepo.current() } returns session(System.currentTimeMillis() + 60_000L)
        guard.ensureValid() // must not throw
    }

    @Test
    fun `isExpired returns true when no session`() {
        every { sessionRepo.current() } returns null
        assertTrue(guard.isExpired())
    }

    @Test
    fun `isExpired returns true when session expired`() {
        every { sessionRepo.current() } returns session(System.currentTimeMillis() - 1000L)
        assertTrue(guard.isExpired())
    }

    @Test
    fun `isExpired returns false when session valid`() {
        every { sessionRepo.current() } returns session(System.currentTimeMillis() + 60_000L)
        assertFalse(guard.isExpired())
    }
}
