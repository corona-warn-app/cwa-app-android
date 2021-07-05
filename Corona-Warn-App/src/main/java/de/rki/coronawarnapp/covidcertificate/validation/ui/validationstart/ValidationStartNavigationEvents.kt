package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation

sealed class ValidationStartNavigationEvents {

    object NavigateToPrivacyFragment : ValidationStartNavigationEvents()
    object NavigateToValidationInfoFragment : ValidationStartNavigationEvents()
    data class ShowTimeMessage(val invalidTime: Boolean) : ValidationStartNavigationEvents()
    data class NavigateToValidationResultFragment(
        val validationResult: DccValidation
    ) : ValidationStartNavigationEvents()
}
