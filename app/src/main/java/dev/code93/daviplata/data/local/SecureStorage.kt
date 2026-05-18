package dev.code93.daviplata.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.code93.daviplata.domain.model.Session
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences by lazy { openEncryptedPrefs() }

    private fun openEncryptedPrefs(): SharedPreferences {
        return try {
            createEncryptedPrefs()
        } catch (t: Throwable) {
            // EncryptedSharedPreferences puede fallar con AEADBadTagException o
            // GeneralSecurityException cuando la master key del Keystore ya no puede
            // descifrar el archivo (reinstalación, auto-backup, restore, cambio de
            // bloqueo de pantalla). En ese caso, recreamos todo desde cero.
            Log.w(TAG, "EncryptedSharedPreferences corrupto, recreando", t)
            resetKeystoreAndPrefs()
            createEncryptedPrefs()
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private fun resetKeystoreAndPrefs() {
        runCatching { context.deleteSharedPreferences(FILE_NAME) }
        runCatching {
            KeyStore.getInstance(ANDROID_KEY_STORE).apply {
                load(null)
                if (containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                    deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                }
            }
        }
    }

    fun saveSession(session: Session) {
        prefs.edit().apply {
            putString(KEY_SESSION_ID, session.sessionId)
            putString(KEY_USER_ID, session.userId)
            putString(KEY_NAME, session.name)
            putString(KEY_PHONE, session.phone)
            putLong(KEY_EXPIRES_AT, session.expiresAtMillis)
        }.apply()
    }

    fun getSession(): Session? {
        val sessionId = prefs.getString(KEY_SESSION_ID, null) ?: return null
        return Session(
            sessionId = sessionId,
            userId = prefs.getString(KEY_USER_ID, "") ?: "",
            name = prefs.getString(KEY_NAME, "") ?: "",
            phone = prefs.getString(KEY_PHONE, "") ?: "",
            expiresAtMillis = prefs.getLong(KEY_EXPIRES_AT, 0L),
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun setExpiresAt(millis: Long) {
        prefs.edit().putLong(KEY_EXPIRES_AT, millis).apply()
    }

    companion object {
        private const val TAG = "SecureStorage"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val FILE_NAME = "daviplata_secure"
        private const val KEY_SESSION_ID = "session.id"
        private const val KEY_USER_ID = "session.userId"
        private const val KEY_NAME = "session.name"
        private const val KEY_PHONE = "session.phone"
        private const val KEY_EXPIRES_AT = "session.expiresAt"
    }
}
