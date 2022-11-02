package de.rki.coronawarnapp.srs.core

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import javax.inject.Inject

@Reusable
class SrsLocalChecker @Inject constructor(
    private val srsSubmissionSettings: SrsSubmissionSettings,
    private val appConfigProvider: AppConfigProvider
) {

    /**
     * @throws SrsSubmissionException
     */
    suspend fun check() {
        // TODo
        throw SrsSubmissionException(SrsSubmissionException.ErrorCode.SUBMISSION_TOO_EARLY)
    }
}
