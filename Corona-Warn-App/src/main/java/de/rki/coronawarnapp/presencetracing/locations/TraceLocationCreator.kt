package de.rki.coronawarnapp.presencetracing.locations

import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import okio.ByteString
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

        // cryptographic seed is a sequence of 16 random bytes
        val cryptographicSeed = ByteArray(16).apply { secureRandom.nextBytes(this) }.toByteString()

        val traceLocation = traceLocationUserInput.toTraceLocation(cryptographicSeed, cnPublicKey)
        return repository.addTraceLocation(traceLocation)
    }
}

fun TraceLocationUserInput.toTraceLocation(cryptographicSeed: ByteString, cnPublicKey: String) = TraceLocation(
    type = type,
    description = description,
    address = address,
    startDate = startDate,
    endDate = endDate,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    cryptographicSeed = cryptographicSeed,
    cnPublicKey = cnPublicKey
)
