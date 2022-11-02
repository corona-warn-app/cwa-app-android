package de.rki.coronawarnapp.srs.core

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
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
        val appConfig = appConfigProvider.getAppConfig()
        when (appConfig.deviceTimeState) {
            ConfigData.DeviceTimeState.ASSUMED_CORRECT -> throw SrsSubmissionException(ErrorCode.DEVICE_TIME_UNVERIFIED)
            ConfigData.DeviceTimeState.INCORRECT -> throw SrsSubmissionException(ErrorCode.DEVICE_TIME_INCORRECT)
            ConfigData.DeviceTimeState.CORRECT ->
        }

        // TODo
        throw SrsSubmissionException(ErrorCode.SUBMISSION_TOO_EARLY)
    }
}
