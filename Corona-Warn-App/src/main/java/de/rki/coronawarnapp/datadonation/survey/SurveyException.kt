package de.rki.coronawarnapp.datadonation.survey

class SurveyException constructor(
    val type: Type,
    message: String?,
    cause: Throwable?
) : Exception("$type: $message", cause) {

    enum class Type {
        ALREADY_PARTICIPATED_THIS_MONTH
    }
}
