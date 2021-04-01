package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import okio.ByteString.Companion.toByteString
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceLocationCreator @Inject constructor(
    private val repository: TraceLocationRepository,
    private val secureRandom: SecureRandom,
    private val environmentSetup: EnvironmentSetup
) {

    suspend fun createTraceLocation(traceLocationUserInput: TraceLocationUserInput): TraceLocation {
        val cnPublicKey = environmentSetup.crowdNotifierPublicKey
        val traceLocation = traceLocationUserInput.toTraceLocation(secureRandom, cnPublicKey)
        return repository.addTraceLocation(traceLocation)
    }
}

fun TraceLocationUserInput.toTraceLocation(secureRandom: SecureRandom, cnPublicKey: String) = TraceLocation(
    type = type,
    description = description,
    address = address,
    startDate = startDate,
    endDate = endDate,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    // cryptographic seed is a sequence of 16 random bytes
    cryptographicSeed = ByteArray(16).apply { secureRandom.nextBytes(this) }.toByteString(),
    cnPublicKey = cnPublicKey
)
