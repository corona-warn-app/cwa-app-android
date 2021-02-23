package de.rki.coronawarnapp.datadonation.storage

import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OTPRepository @Inject constructor(
    private val surveySettings: SurveySettings
) {

    val otp: OneTimePassword?
        get() = surveySettings.oneTimePassword

    var otpAuthorizationResult: OTPAuthorizationResult?
        get() = surveySettings.otpAuthorizationResult
        set(value) {
            surveySettings.otpAuthorizationResult = value
            // since we have a result from authorization server we must not use the generated otp again
            surveySettings.oneTimePassword = null
        }

    fun generateOTP(): OneTimePassword = OneTimePassword().also {
        surveySettings.oneTimePassword = it
        // only one otp can be stored at a time - remove authorization result of older otp
        surveySettings.otpAuthorizationResult = null
    }

    fun clear() {
        surveySettings.oneTimePassword = null
        surveySettings.otpAuthorizationResult = null
    }
}
