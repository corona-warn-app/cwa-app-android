package de.rki.coronawarnapp.submission

class Symptoms(
    val startOfSymptoms: StartOfSymptoms?,
    val symptomIndication: SymptomIndication
) {
    sealed class StartOfSymptoms {

        data class Date(val millis: Long) : StartOfSymptoms()
        object LastSevenDays : StartOfSymptoms()
        object OneToTwoWeeksAgo : StartOfSymptoms()
        object MoreThanTwoWeeks : StartOfSymptoms()
        object NoInformation : StartOfSymptoms()
    }

    enum class SymptomIndication {
        POSITIVE,
        NEGATIVE,
        NO_INFORMATION
    }
}
