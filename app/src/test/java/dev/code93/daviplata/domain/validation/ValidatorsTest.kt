package dev.code93.daviplata.domain.validation

import dev.code93.daviplata.domain.validation.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    // ─── phone ─────────────────────────────────────────────────────────────

    @Test fun `phone valid 10 digits`() = assertNull(Validators.phone("3001234567"))

    @Test fun `phone too short`() {
        assertNotNull(Validators.phone("300123"))
    }

    @Test fun `phone too long`() {
        assertNotNull(Validators.phone("30012345678"))
    }

    @Test fun `phone with letters rejected`() {
        assertNotNull(Validators.phone("300abc4567"))
    }

    // ─── password ──────────────────────────────────────────────────────────

    @Test fun `password valid at min length`() = assertNull(Validators.password("12345678"))

    @Test fun `password too short`() {
        assertNotNull(Validators.password("1234567"))
    }

    // ─── strongPassword ────────────────────────────────────────────────────

    @Test fun `strong password rejects when missing uppercase`() {
        assertNotNull(Validators.strongPassword("password1"))
    }

    @Test fun `strong password rejects when missing digit`() {
        assertNotNull(Validators.strongPassword("Password"))
    }

    @Test fun `strong password accepts valid case`() {
        assertNull(Validators.strongPassword("Password1"))
    }

    @Test fun `strong password forwards length error first`() {
        // El error debe ser de longitud, no de uppercase/digit, cuando ambos fallan.
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres",
            Validators.strongPassword("abc"),
        )
    }

    // ─── document / email / username / amount ──────────────────────────────

    @Test fun `document range`() {
        assertNotNull(Validators.document("123"))      // muy corto
        assertNull(Validators.document("123456"))
        assertNull(Validators.document("1234567890"))
        assertNotNull(Validators.document("12345678901")) // muy largo
    }

    @Test fun `email needs at`() {
        assertNotNull(Validators.email("invalid"))
        assertNull(Validators.email("a@b"))
    }

    @Test fun `username pattern`() {
        assertNotNull(Validators.username(""))
        assertNotNull(Validators.username("user name")) // espacio prohibido
        assertNull(Validators.username("fabian.ardila"))
    }

    @Test fun `amount must be positive`() {
        assertNotNull(Validators.amount(0.0))
        assertNotNull(Validators.amount(-1.0))
        assertNull(Validators.amount(0.01))
    }

    // ─── passwordConfirmation ──────────────────────────────────────────────

    @Test fun `password confirmation mismatch`() {
        assertNotNull(Validators.passwordConfirmation("Password1", "Password2"))
    }

    @Test fun `password confirmation match`() {
        assertNull(Validators.passwordConfirmation("Password1", "Password1"))
    }

    // ─── firstError ────────────────────────────────────────────────────────

    @Test fun `firstError returns first non-null in order`() {
        val msg = Validators.firstError(null, "primer error", null, "segundo error")
        assertEquals("primer error", msg)
    }

    @Test fun `firstError returns null when all pass`() {
        assertNull(Validators.firstError(null, null, null))
    }

    // ─── predicates ────────────────────────────────────────────────────────

    @Test fun `predicates work`() {
        assertTrue(Validators.hasUppercase("Abc"))
        assertTrue(Validators.hasDigit("abc1"))
        assertTrue(Validators.hasSymbol("abc!"))
    }
}
