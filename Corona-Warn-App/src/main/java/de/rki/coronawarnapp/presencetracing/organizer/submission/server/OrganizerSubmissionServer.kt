package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass

class OrganizerSubmissionServer {
    suspend fun submit(
        tan: String,
        payload: SubmissionPayloadOuterClass.SubmissionPayload
    ) {
        // TODO
    }
}
