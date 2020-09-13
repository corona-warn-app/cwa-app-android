package de.rki.coronawarnapp.submission

sealed class StartOfSymptoms {

    data class Date(val date: Int) : StartOfSymptoms()
    object LastSevenDays : StartOfSymptoms()
    object OneToTwoWeeksAgo : StartOfSymptoms()
    object MoreThanTwoWeeks : StartOfSymptoms()
    object NoInformation : StartOfSymptoms()
}
