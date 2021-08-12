package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass
import javax.inject.Inject

class OrganizerSubmissionServer @Inject constructor(
    private val organizerSubmissionApi: OrganizerSubmissionApi
) {
    suspend fun submit(
        tan: String,
        payload: SubmissionPayloadOuterClass.SubmissionPayload
    ) {
        organizerSubmissionApi.submit(tan, payload)
    }
}
