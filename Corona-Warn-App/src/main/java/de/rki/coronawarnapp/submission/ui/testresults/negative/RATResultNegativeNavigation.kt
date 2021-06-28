package de.rki.coronawarnapp.submission.ui.testresults.negative

sealed class RATResultNegativeNavigation {
    object Back : RATResultNegativeNavigation()
    object ShowDeleteWarning : RATResultNegativeNavigation()
}
