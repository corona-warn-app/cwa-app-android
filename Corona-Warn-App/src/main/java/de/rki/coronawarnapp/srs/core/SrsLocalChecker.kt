package de.rki.coronawarnapp.srs.core

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.ConfigData.DeviceTimeState
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject

@Reusable
class SrsLocalChecker @Inject constructor(
    private val srsSubmissionSettings: SrsSubmissionSettings,
    private val appConfigProvider: AppConfigProvider,
    private val cwaSettings: CWASettings,
    private val timeStamper: TimeStamper,
) {

    /**
     * @throws SrsSubmissionException
     */
    suspend fun check() {
        val appConfig = appConfigProvider.getAppConfig()
        val deviceTimeState = appConfig.deviceTimeState

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
        val onboardingInHours = appConfig.selfReportSubmission.common.timeSinceOnboardingInHours
        if (reliableDuration < onboardingInHours) {
            Timber.e(
                "TIME since onboarding is unverified reliableDuration=%s, configDuration=%s",
                reliableDuration,
                onboardingInHours
            )
            throw  SrsSubmissionException(ErrorCode.TIME_SINCE_ONBOARDING_UNVERIFIED)
        }
    }
}
