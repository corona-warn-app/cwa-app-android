package de.rki.coronawarnapp.familytest.ui.consent

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode

sealed class FamilyTestConsentNavigationEvents {

    data class NavigateToCertificateRequest(
        val coronaTestQRCode: CoronaTestQRCode,
        val consentGiven: Boolean,
        val allowReplacement: Boolean
    ) : FamilyTestConsentNavigationEvents()

    object NavigateToDataPrivacy : FamilyTestConsentNavigationEvents()

    object NavigateBack : FamilyTestConsentNavigationEvents()
}
