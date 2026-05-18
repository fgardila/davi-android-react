package dev.code93.daviplata.domain.validation

object Validators {

    const val PHONE_LENGTH = 10
    const val PASSWORD_MIN_LENGTH = 8
    const val DOCUMENT_MIN_LENGTH = 6
    const val DOCUMENT_MAX_LENGTH = 10
    private val USERNAME_REGEX = Regex("[a-zA-Z0-9.]+")

    fun phone(phone: String): String? = when {
        phone.length != PHONE_LENGTH -> "El número celular debe tener $PHONE_LENGTH dígitos"
        !phone.all(Char::isDigit) -> "El número celular solo puede contener dígitos"
        else -> null
    }

    fun password(password: String): String? =
        if (password.length < PASSWORD_MIN_LENGTH)
            "La contraseña debe tener al menos $PASSWORD_MIN_LENGTH caracteres"
        else null

    fun strongPassword(password: String): String? {
        password(password)?.let { return it }
        return when {
            !hasUppercase(password) -> "La contraseña debe tener al menos una mayúscula"
            !hasDigit(password) -> "La contraseña debe tener al menos un número"
            else -> null
        }
    }

    fun passwordConfirmation(password: String, confirmation: String): String? =
        if (password != confirmation) "Las contraseñas no coinciden" else null

    fun name(name: String): String? =
        if (name.isBlank()) "El nombre no puede estar vacío" else null

    fun document(document: String): String? =
        if (document.length !in DOCUMENT_MIN_LENGTH..DOCUMENT_MAX_LENGTH)
            "El documento debe tener entre $DOCUMENT_MIN_LENGTH y $DOCUMENT_MAX_LENGTH dígitos"
        else null

    fun email(email: String): String? =
        if (!email.contains("@")) "El correo electrónico no es válido" else null

    fun username(username: String): String? = when {
        username.isBlank() -> "El usuario no puede estar vacío"
        !username.matches(USERNAME_REGEX) -> "El usuario solo puede contener letras, números y puntos"
        else -> null
    }

    fun amount(amount: Double): String? =
        if (amount <= 0.0) "El monto debe ser mayor a 0" else null

    fun hasUppercase(password: String): Boolean = password.any { it.isUpperCase() }
    fun hasDigit(password: String): Boolean = password.any { it.isDigit() }
    fun hasSymbol(password: String): Boolean = password.any { !it.isLetterOrDigit() }
    fun firstError(vararg checks: String?): String? = checks.firstOrNull { it != null }
}
