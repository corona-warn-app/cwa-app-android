package de.rki.coronawarnapp.presencetracing.organizer.submission

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.OrganizerCheckInsTransformer
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.OrganizerSubmissionServer
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class OrganizerSubmissionRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val organizerCheckInsTransformer: OrganizerCheckInsTransformer,
    private val verificationServer: VerificationServer,
    private val organizerSubmissionServer: OrganizerSubmissionServer,
) {

    /**
     * Submits event organizer check-ins
     * @throws OrganizerSubmissionException in case of failure
     */
    suspend fun submit(payload: OrganizerSubmissionPayload) {
        organizerCheckInsTransformer.transform(listOf(payload.toCheckIns()))
    }
}

fun OrganizerSubmissionPayload.toCheckIns(): CheckIn {
    val payload = this

    return with(traceLocation) {
        CheckIn(
            id = id,
            traceLocationId = locationId,
            version = version,
            type = traceLocation.type.number,
            description = description,
            address = address,
            traceLocationStart = startDate,
            traceLocationEnd = endDate,
            defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
            cryptographicSeed = cryptographicSeed,
            cnPublicKey = cnPublicKey,
            checkInStart = payload.startDate,
            checkInEnd = payload.endDate,
            completed = true,
            createJournalEntry = false,
            isSubmitted = false,
            hasSubmissionConsent = true
        )
    }
}

data class OrganizerSubmissionPayload(
    val traceLocation: TraceLocation,
    val startDate: Instant,
    val endDate: Instant,
    val tan: String
)
