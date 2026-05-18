package dev.code93.daviplata.domain.usecase.session

import dev.code93.daviplata.domain.repository.SessionRepository
import javax.inject.Inject

class ClearSessionUseCase @Inject constructor(
    private val sessionRepo: SessionRepository,
) {
    suspend operator fun invoke() = sessionRepo.clear()
}
