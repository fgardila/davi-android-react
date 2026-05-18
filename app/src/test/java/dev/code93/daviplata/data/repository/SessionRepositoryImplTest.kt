package dev.code93.daviplata.data.repository

import dev.code93.daviplata.data.local.SecureStorage
import dev.code93.daviplata.data.repository.SessionRepositoryImpl
import dev.code93.daviplata.domain.model.Session
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRepositoryImplTest {

    private fun session(expiresAtMillis: Long = System.currentTimeMillis() + 60_000L) = Session(
        sessionId = "sid", userId = "u-001", name = "Fabian",
        phone = "3001234567", expiresAtMillis = expiresAtMillis,
    )

    @Test
    fun `current() returns the session loaded from storage at construction`() {
        val storage: SecureStorage = mockk(relaxed = true)
        val s = session()
        every { storage.getSession() } returns s

        val repo = SessionRepositoryImpl(storage)

        assertEquals(s, repo.current())
    }

    @Test
    fun `current() is null when storage is empty`() {
        val storage: SecureStorage = mockk(relaxed = true)
        every { storage.getSession() } returns null

        val repo = SessionRepositoryImpl(storage)

        assertNull(repo.current())
    }

    @Test
    fun `save() updates storage AND in-memory cache atomically`() = runTest {
        val storage: SecureStorage = mockk(relaxed = true)
        every { storage.getSession() } returns null
        val repo = SessionRepositoryImpl(storage)

        val s = session()
        repo.save(s)

        verify { storage.saveSession(s) }
        assertEquals(s, repo.current())
        assertEquals(s, repo.sessionFlow.first())
    }

    @Test
    fun `clear() wipes storage AND in-memory cache`() = runTest {
        val storage: SecureStorage = mockk(relaxed = true)
        every { storage.getSession() } returns session()
        val repo = SessionRepositoryImpl(storage)

        repo.clear()

        verify { storage.clear() }
        assertNull(repo.current())
        assertNull(repo.sessionFlow.first())
    }

    @Test
    fun `isValid() false when no session`() {
        val storage: SecureStorage = mockk(relaxed = true)
        every { storage.getSession() } returns null
        val repo = SessionRepositoryImpl(storage)

        assertFalse(repo.isValid())
    }

    @Test
    fun `isValid() false when expired`() {
        val storage: SecureStorage = mockk(relaxed = true)
        every { storage.getSession() } returns session(System.currentTimeMillis() - 1000L)
        val repo = SessionRepositoryImpl(storage)

        assertFalse(repo.isValid())
    }

    @Test
    fun `isValid() true when not expired`() {
        val storage: SecureStorage = mockk(relaxed = true)
        every { storage.getSession() } returns session(System.currentTimeMillis() + 60_000L)
        val repo = SessionRepositoryImpl(storage)

        assertTrue(repo.isValid())
    }

    @Test
    fun `sessionFlow emits initial value from storage`() = runTest {
        val storage: SecureStorage = mockk(relaxed = true)
        val s = session()
        every { storage.getSession() } returns s
        val repo = SessionRepositoryImpl(storage)

        assertEquals(s, repo.sessionFlow.first())
    }
}
