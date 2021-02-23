package de.rki.coronawarnapp.datadonation.survey

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError

class SurveyException constructor(
    val type: Type,
    message: String? = null,
    cause: Throwable? = null
) : Exception("$type: $message", cause), HasHumanReadableError {

    override fun toHumanReadableError(context: Context): HumanReadableError {
        val messageRes = when (type) {
            Type.ALREADY_PARTICIPATED_THIS_MONTH ->
                R.string.datadonation_details_survey_consent_error_ALREADY_PARTICIPATED
            Type.OTP_NOT_AUTHORIZED -> R.string.datadonation_details_survey_consent_error_TRY_AGAIN_LATER
        }
        return HumanReadableError(description = context.getString(messageRes, type))
    }

    enum class Type {
        ALREADY_PARTICIPATED_THIS_MONTH,
        OTP_NOT_AUTHORIZED
    }
}
