package de.rki.coronawarnapp.srs.core

import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class SubmissionReporter @Inject constructor(
    private val srsSubmissionSettings: SrsSubmissionSettings,
    private val contactDiaryRepository: ContactDiaryRepository,
) {

    suspend fun reportAt(time: Instant) {
        Timber.d("reportAt($time)")
        srsSubmissionSettings.setMostRecentSubmissionTime(time)
        contactDiaryRepository.insertSubmissionAt(time)
    }
}
