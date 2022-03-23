package de.rki.coronawarnapp.familytest.ui.selection

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode

sealed class TestRegistrationSelectionNavigationEvents {

    data class NavigateToPerson(
        val coronaTestQRCode: CoronaTestQRCode
    ) : TestRegistrationSelectionNavigationEvents()

    data class NavigateToFamily(
        val coronaTestQRCode: CoronaTestQRCode
    ) : TestRegistrationSelectionNavigationEvents()

    data class NavigateToDeletionWarning(
        val testRegistrationRequest: TestRegistrationRequest
    ) : TestRegistrationSelectionNavigationEvents()

    object NavigateBack : TestRegistrationSelectionNavigationEvents()
}
