package dev.code93.daviplata.security

import at.favre.lib.crypto.bcrypt.BCrypt
import javax.inject.Inject

class PasswordHasher @Inject constructor() {
    fun hash(password: String): String =
        BCrypt.withDefaults().hashToString(10, password.toCharArray())

    fun verify(password: String, hash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}
