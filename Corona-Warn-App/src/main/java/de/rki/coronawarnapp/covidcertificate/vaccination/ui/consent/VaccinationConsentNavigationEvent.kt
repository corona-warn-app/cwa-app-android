package de.rki.coronawarnapp.covidcertificate.vaccination.ui.consent

sealed class VaccinationConsentNavigationEvent {
    object NavigateToDataPrivacy : VaccinationConsentNavigationEvent()
    object NavigateToCertificates : VaccinationConsentNavigationEvent()
}
