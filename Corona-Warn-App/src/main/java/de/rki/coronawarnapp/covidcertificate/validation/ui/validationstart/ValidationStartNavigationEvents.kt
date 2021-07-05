package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

sealed class ValidationStartNavigationEvents {

    object NavigateToPrivacyFragment : ValidationStartNavigationEvents()
    object NavigateToValidationInfoFragment : ValidationStartNavigationEvents()
    object NavigateToValidationResultFragment : ValidationStartNavigationEvents()
}
