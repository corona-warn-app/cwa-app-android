package de.rki.coronawarnapp.submission

data class Symptoms(
    val startOfSymptoms: StartOf?,
    val symptomIndication: Indication
) {
    sealed class StartOf {

        data class Date(val millis: Long) : StartOf()
        object LastSevenDays : StartOf()
        object OneToTwoWeeksAgo : StartOf()
        object MoreThanTwoWeeks : StartOf()
        object NoInformation : StartOf()
    }

    enum class Indication {
        POSITIVE,
        NEGATIVE,
        NO_INFORMATION
    }
}
