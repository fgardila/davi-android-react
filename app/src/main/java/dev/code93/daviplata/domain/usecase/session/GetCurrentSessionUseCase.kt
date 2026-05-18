package dev.code93.daviplata.domain.usecase.session

import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.repository.SessionRepository
import javax.inject.Inject

class GetCurrentSessionUseCase @Inject constructor(
    private val sessionRepo: SessionRepository,
) {
    operator fun invoke(): Session? = sessionRepo.current()
}
