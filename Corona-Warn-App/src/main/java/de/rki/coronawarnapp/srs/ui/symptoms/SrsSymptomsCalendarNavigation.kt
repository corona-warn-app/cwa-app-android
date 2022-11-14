package de.rki.coronawarnapp.srs.ui.symptoms

sealed class SrsSymptomsCalendarNavigation {

    object ShowCloseDialog : SrsSymptomsCalendarNavigation()

    object GoToHome : SrsSymptomsCalendarNavigation()

    object GoToThankYouScreen : SrsSymptomsCalendarNavigation()

    object ShowSubmissionWarning : SrsSymptomsCalendarNavigation()
}
