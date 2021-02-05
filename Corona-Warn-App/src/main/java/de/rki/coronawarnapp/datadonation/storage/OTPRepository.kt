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
        get() = surveySettings.oneTimePassword

    fun generateOTP(): OneTimePassword = OneTimePassword().also {
        surveySettings.oneTimePassword = it
    }

    fun clear() {
        surveySettings.oneTimePassword = null
    }
}
