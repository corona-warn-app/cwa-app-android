package de.rki.coronawarnapp.submission

import org.joda.time.Instant

class SubmissionStatus(
    val timestamp: Instant,
    val succeeded: Boolean,
    val transmissionRiskVector: TransmissionRiskVector
)
