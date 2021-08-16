package de.rki.coronawarnapp.presencetracing.organizer.submission

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.playbook.OrganizerPlaybook
import de.rki.coronawarnapp.presencetracing.checkins.OrganizerCheckInsTransformer
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.OrganizerSubmissionServer
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Reusable
class OrganizerSubmissionRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val checkInsTransformer: OrganizerCheckInsTransformer,
    private val organizerPlaybook: OrganizerPlaybook,
) {

    /**
     * Submits event organizer check-ins
     * @throws OrganizerSubmissionException in case of failure
     */
    suspend fun submit(payload: OrganizerSubmissionPayload) =
        withContext(appScope.coroutineContext) {
            // Prepare CheckIns for submission
            val checkInsReport = checkInsTransformer.transform(listOf(payload.toCheckIns()))
            organizerPlaybook.submit(payload.tan, checkInsReport)
        }
}
