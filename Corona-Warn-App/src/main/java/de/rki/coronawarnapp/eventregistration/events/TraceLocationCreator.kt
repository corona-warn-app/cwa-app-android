package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import okio.ByteString.Companion.toByteString
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

const val TRACE_LOCATION_VERSION = 1

@Singleton
class TraceLocationCreator @Inject constructor(
    private val repository: TraceLocationRepository,
    private val secureRandom: SecureRandom
) {

    // cryptographic seed is a sequence of 16 random bytes
    private fun generateCryptographicSeed(): ByteArray = ByteArray(16).apply {
        secureRandom.nextBytes(this)
    }

    suspend fun createTraceLocation(traceLocationUserInput: TraceLocationUserInput): TraceLocation {
        val traceLocationToStore = with(traceLocationUserInput) {
            TraceLocation(
                type = type,
                description = description,
                address = address,
                startDate = startDate,
                endDate = endDate,
                defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
                cryptographicSeed = generateCryptographicSeed().toByteString(),
                cnPublicKey = "hardcoded public key TODO: replace with real one",
                version = TRACE_LOCATION_VERSION
            )
        }

        return repository.addTraceLocation(traceLocationToStore)
    }
}
