package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode

sealed class DeepLinkDirections {
    data class GoToCheckInsFragment(val uriString: String) : DeepLinkDirections()
    data class GoToDeletionScreen(val request: TestRegistrationRequest) : DeepLinkDirections()
    data class GoToSubmissionConsentFragment(val request: CoronaTestQRCode) : DeepLinkDirections()
    data class Error(val error: Throwable) : DeepLinkDirections()
}
