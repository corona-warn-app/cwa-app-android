package de.rki.coronawarnapp.datadonation.storage

import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OTPRepository @Inject constructor(
    private val surveySettings: SurveySettings
) {
    fun getOtpAuthorizationResult() = surveySettings.otpAuthorizationResult

    suspend fun updateOtpAuthorizationResult(result: OTPAuthorizationResult?) {
        surveySettings.updateOtpAuthorizationResult(result)
        surveySettings.updateOneTimePassword(null)
    }

    suspend fun getOtp() = surveySettings.oneTimePassword.first()

    suspend fun generateOTP(): OneTimePassword = OneTimePassword().also {
        surveySettings.updateOneTimePassword(it)
        // only one otp can be stored at a time - remove authorization result of older otp
        surveySettings.updateOtpAuthorizationResult(null)
    }

    suspend fun clear() {
        surveySettings.reset()
    }
}
