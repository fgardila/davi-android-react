package dev.code93.daviplata.data.repository

import dev.code93.daviplata.data.local.SecureStorage
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val storage: SecureStorage,
) : SessionRepository {

    private val _sessionFlow = MutableStateFlow<Session?>(storage.getSession())
    override val sessionFlow: Flow<Session?> = _sessionFlow.asStateFlow()

    override fun current(): Session? = _sessionFlow.value

    override suspend fun save(session: Session) {
        storage.saveSession(session)
        _sessionFlow.value = session
    }

    override suspend fun clear() {
        storage.clear()
        _sessionFlow.value = null
    }

    override fun isValid(): Boolean {
        val session = _sessionFlow.value ?: return false
        return System.currentTimeMillis() < session.expiresAtMillis
    }
}
