package de.rki.coronawarnapp.presencetracing.organizer.submission

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.presencetracing.checkins.OrganizerCheckInsTransformer
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.OrganizerSubmissionPayload
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.OrganizerSubmissionServer
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.toCheckIns
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
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
    suspend fun submit(payload: OrganizerSubmissionPayload) =
        withContext(appScope.coroutineContext) {
            // Prepare CheckIns for submission
            val checkInsReport = organizerCheckInsTransformer.transform(listOf(payload.toCheckIns()))
            // Obtain registration token
            val registrationRequest = RegistrationRequest(key = payload.tan, type = VerificationKeyType.TELETAN)
            val registrationToken = verificationServer.retrieveRegistrationToken(registrationRequest)
            // Obtain upload TAN
            val uploadTAN = verificationServer.retrieveTan(registrationToken)
            organizerSubmissionServer.submit(uploadTAN, checkInsReport)
        }
}
