package de.rki.coronawarnapp.srs.core

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData.DeviceTimeState
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject

@Reusable
class SrsLocalChecker @Inject constructor(
    private val srsSubmissionSettings: SrsSubmissionSettings,
    private val srsDevSettings: SrsDevSettings,
    private val appConfigProvider: AppConfigProvider,
    private val cwaSettings: CWASettings,
    private val timeStamper: TimeStamper,
) {

    /**
     * Check SRS local time prerequisites
     * throws an error if it fails, otherwise does nothing
     * @throws SrsSubmissionException
     */
    suspend fun check() {
        if (!srsDevSettings.checkLocalPrerequisites()) {
            Timber.d("checkLocalPrerequisites is disabled")
            return
        }

        val appConfig = appConfigProvider.getAppConfig()
        val deviceTimeState = appConfig.deviceTimeState
        val selfReportSubmissionCommon = appConfig.selfReportSubmission.common

        if (deviceTimeState == DeviceTimeState.INCORRECT) {
            Timber.e("DeviceTime INCORRECT")
            throw SrsSubmissionException(ErrorCode.DEVICE_TIME_INCORRECT)
        }

        if (deviceTimeState == DeviceTimeState.ASSUMED_CORRECT) {
            Timber.e("DeviceTime ASSUMED_CORRECT")
            throw SrsSubmissionException(ErrorCode.DEVICE_TIME_UNVERIFIED)
        }

        val reliableDuration = Duration.between(
            cwaSettings.firstReliableDeviceTime.first(),
            timeStamper.nowUTC
        )
        val onboardingInHours = selfReportSubmissionCommon.timeSinceOnboardingInHours
        if (reliableDuration < onboardingInHours) {
            Timber.e(
                "Time since onboarding is unverified reliableDuration=%s, configDuration=%s",
                reliableDuration,
                onboardingInHours
            )
            throw SrsSubmissionException(
                errorCode = ErrorCode.MIN_TIME_SINCE_ONBOARDING,
                errorArgs = arrayOf(onboardingInHours.toHours(), onboardingInHours.toHours())
            )
        }

        val durationSinceSubmission = Duration.between(
            srsSubmissionSettings.getMostRecentSubmissionTime(),
            timeStamper.nowUTC
        )

        val submissionsInDays = selfReportSubmissionCommon.timeBetweenSubmissionsInDays
        if (durationSinceSubmission < submissionsInDays) {
            Timber.e(
                "Submission is too early durationSinceSubmission=%s, configDuration=%s",
                durationSinceSubmission,
                submissionsInDays
            )
            throw SrsSubmissionException(
                errorCode = ErrorCode.SUBMISSION_TOO_EARLY,
                errorArgs = arrayOf(submissionsInDays.toDays())
            )
        }

        Timber.d("Local prerequisites are met -> Congratulations!")
    }
}
