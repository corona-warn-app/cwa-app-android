package de.rki.coronawarnapp.datadonation.storage

import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OTPRepository @Inject constructor(
    private val surveySettings: SurveySettings
) {

    val lastOTP: OneTimePassword?
        get() = surveySettings.oneTimePassword.value

    fun generateOTP(): OneTimePassword = OneTimePassword().also {
        surveySettings.oneTimePassword.update { it }
    }

    fun clear() {
        surveySettings.oneTimePassword.update { null }
    }
}
