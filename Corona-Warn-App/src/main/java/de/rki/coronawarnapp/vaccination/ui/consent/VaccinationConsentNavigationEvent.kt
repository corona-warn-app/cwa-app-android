package de.rki.coronawarnapp.vaccination.ui.consent

sealed class VaccinationConsentNavigationEvent {
    object NavigateToDataPrivacy : VaccinationConsentNavigationEvent()
    object NavigateToQrCodeScan : VaccinationConsentNavigationEvent()
}
