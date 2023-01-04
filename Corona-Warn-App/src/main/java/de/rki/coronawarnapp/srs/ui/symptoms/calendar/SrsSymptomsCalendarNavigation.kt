package de.rki.coronawarnapp.srs.ui.symptoms.calendar

sealed class SrsSymptomsCalendarNavigation {

    object ShowCloseDialog : SrsSymptomsCalendarNavigation()

    object GoToHome : SrsSymptomsCalendarNavigation()

    object GoToThankYouScreen : SrsSymptomsCalendarNavigation()

    data class TruncatedSubmission(val numberOfDays: String?) : SrsSymptomsCalendarNavigation()
    data class Error(val cause: Exception) : SrsSymptomsCalendarNavigation()

    object ShowSubmissionWarning : SrsSymptomsCalendarNavigation()
}
