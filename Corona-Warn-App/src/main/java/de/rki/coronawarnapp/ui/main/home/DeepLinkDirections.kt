package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest

sealed class DeepLinkDirections {
    data class GoToCheckInsFragment(val uriString: String) : DeepLinkDirections()

    data class GoToDeletionScreen(val qrCode: TestRegistrationRequest) : DeepLinkDirections()

    data class GoToSubmissionConsentFragment(val qrCodeRawString: String) : DeepLinkDirections()
}
