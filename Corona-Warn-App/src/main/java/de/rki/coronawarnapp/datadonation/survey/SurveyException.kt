package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.R

class SurveyException constructor(
    val type: Type,
    message: String? = null,
    cause: Throwable? = null
) : Exception("$type: $message", cause) {

    enum class Type {
        ALREADY_PARTICIPATED_THIS_MONTH,
        OTP_NOT_AUTHORIZED
    }
}

fun SurveyException.errorMsgRes(): Int = when (type) {
    SurveyException.Type.ALREADY_PARTICIPATED_THIS_MONTH ->
        R.string.datadonation_details_survey_consent_error_ALREADY_PARTICIPATED
    else -> R.string.datadonation_details_survey_consent_error_TRY_AGAIN_LATER
}
