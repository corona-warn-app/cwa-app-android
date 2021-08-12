package de.rki.coronawarnapp.presencetracing.organizer.submission

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsTransformer
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.OrganizerSubmissionServer
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class OrganizerSubmissionRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val checkInsTransformer: CheckInsTransformer,
    private val verificationServer: VerificationServer,
    private val organizerSubmissionServer: OrganizerSubmissionServer,
) {

    /**
     * Submits event organizer check-ins
     * @throws OrganizerSubmissionException in case of failure
     */
    suspend fun submit(payload: OrganizerSubmissionPayload) {
        // TODO
    }
}

data class OrganizerSubmissionPayload(
    val traceLocation: TraceLocation,
    val startDate: Instant,
    val endDate: Instant,
    val tan: String
)
