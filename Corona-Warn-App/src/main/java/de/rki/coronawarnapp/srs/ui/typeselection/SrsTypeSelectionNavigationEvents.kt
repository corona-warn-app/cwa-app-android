package de.rki.coronawarnapp.srs.ui.typeselection

import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType

sealed class SrsTypeSelectionNavigationEvents {
    object NavigateToMainScreen : SrsTypeSelectionNavigationEvents()
    object NavigateToCloseDialog : SrsTypeSelectionNavigationEvents()
    data class NavigateToShareCheckins(
        val type: SrsSubmissionType
    ) : SrsTypeSelectionNavigationEvents()

    data class NavigateToShareSymptoms(
        val type: SrsSubmissionType
    ) : SrsTypeSelectionNavigationEvents()
}
