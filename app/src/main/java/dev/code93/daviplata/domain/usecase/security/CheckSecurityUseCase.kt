package dev.code93.daviplata.domain.usecase.security

import dev.code93.daviplata.BuildConfig
import dev.code93.daviplata.security.EmulatorDetector
import dev.code93.daviplata.security.RootDetector
import javax.inject.Inject

enum class SecurityStatus { SAFE, ROOTED, EMULATOR_NOT_DEBUG }

class CheckSecurityUseCase @Inject constructor(
    private val rootDetector: RootDetector,
    private val emulatorDetector: EmulatorDetector,
) {
    operator fun invoke(): SecurityStatus = when {
        rootDetector.isRooted() -> SecurityStatus.ROOTED
        emulatorDetector.isEmulator() && !BuildConfig.DEBUG -> SecurityStatus.EMULATOR_NOT_DEBUG
        else -> SecurityStatus.SAFE
    }
}
