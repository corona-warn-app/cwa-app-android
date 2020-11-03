package de.rki.coronawarnapp.submission

import org.joda.time.LocalDate

data class Symptoms(
    val startOfSymptoms: StartOf?,
    val symptomIndication: Indication
) {
    sealed class StartOf {

        data class Date(val date: LocalDate) : StartOf()
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
