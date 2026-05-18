package dev.code93.daviplata.domain.repository

import dev.code93.daviplata.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val sessionFlow: Flow<Session?>
    fun current(): Session?
    suspend fun save(session: Session)
    suspend fun clear()
    fun isValid(): Boolean
}
