package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import org.joda.time.Instant

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
