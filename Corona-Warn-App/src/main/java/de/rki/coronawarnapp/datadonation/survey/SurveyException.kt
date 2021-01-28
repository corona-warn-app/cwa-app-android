package de.rki.coronawarnapp.datadonation.survey

class SurveyException constructor(
    message: String?,
    cause: Throwable?
) : Exception(message, cause)
