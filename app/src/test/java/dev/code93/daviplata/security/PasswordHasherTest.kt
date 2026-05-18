package dev.code93.daviplata.security

import dev.code93.daviplata.security.PasswordHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    private val hasher = PasswordHasher()

    @Test
    fun `verify accepts the original password`() {
        val hash = hasher.hash("demo1234")
        assertTrue(hasher.verify("demo1234", hash))
    }

    @Test
    fun `verify rejects a different password`() {
        val hash = hasher.hash("demo1234")
        assertFalse(hasher.verify("wrong1234", hash))
    }

    @Test
    fun `hash uses salt - same password produces different hashes`() {
        val a = hasher.hash("demo1234")
        val b = hasher.hash("demo1234")
        // BCrypt embeds a random salt → mismos inputs producen hashes distintos.
        assertNotEquals(a, b)
        // ...pero ambos verifican el password original.
        assertTrue(hasher.verify("demo1234", a))
        assertTrue(hasher.verify("demo1234", b))
    }

    @Test
    fun `hash output is a valid BCrypt string`() {
        val hash = hasher.hash("demo1234")
        // Formato BCrypt: $2a|$2b|$2y$<cost>$<22 chars salt><31 chars hash>, total 60 chars.
        assertTrue(hash.startsWith("\$2"))
        assertTrue(hash.length == 60)
    }

    @Test
    fun `verify rejects empty against a real hash`() {
        val hash = hasher.hash("demo1234")
        assertFalse(hasher.verify("", hash))
    }
}
