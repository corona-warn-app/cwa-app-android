package de.rki.coronawarnapp.presencetracing.organizer.submission

import dagger.Reusable
import de.rki.coronawarnapp.playbook.OrganizerPlaybook
import de.rki.coronawarnapp.presencetracing.checkins.OrganizerCheckInsTransformer
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
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
            Timber.tag(TAG).d("submit(payload=%s)", payload)

            // Prepare CheckIns for submission
            val checkInsReport = checkInsTransformer.transform(listOf(payload.toCheckIn()))

            Timber.tag(TAG).d("checkInsReport=%s", checkInsReport)
            organizerPlaybook.submit(payload.tan, checkInsReport)

            Timber.tag(TAG).d("organizer submission passed")
        }

    companion object {
        private val TAG = OrganizerSubmissionRepository::class.simpleName
    }
}
