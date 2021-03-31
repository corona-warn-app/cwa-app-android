package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.toTraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceLocationCreator @Inject constructor(
    private val repository: TraceLocationRepository,
    private val secureRandom: SecureRandom
) {

    suspend fun createTraceLocation(traceLocationUserInput: TraceLocationUserInput): TraceLocation {
        return repository.addTraceLocation(traceLocationUserInput.toTraceLocation(secureRandom))
    }
}
