package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode

sealed class MainActivityEvent {
    data class GoToCheckInsFragment(val uriString: String) : MainActivityEvent()
    data class GoToDeletionScreen(val request: TestRegistrationRequest) : MainActivityEvent()
    data class GoToSubmissionConsentFragment(val request: CoronaTestQRCode) : MainActivityEvent()
    data class Error(val error: Throwable) : MainActivityEvent()
    object OpenScanner : MainActivityEvent()
}
