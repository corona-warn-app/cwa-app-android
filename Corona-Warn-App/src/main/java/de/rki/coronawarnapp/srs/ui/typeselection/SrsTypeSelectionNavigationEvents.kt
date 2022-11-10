package de.rki.coronawarnapp.srs.ui.typeselection

sealed class SrsTypeSelectionNavigationEvents {
    object NavigateToMainScreen : SrsTypeSelectionNavigationEvents()
    object NavigateToCloseDialog : SrsTypeSelectionNavigationEvents()
    object NavigateToShareCheckins : SrsTypeSelectionNavigationEvents()
    object NavigateToShareSymptoms : SrsTypeSelectionNavigationEvents()
}
