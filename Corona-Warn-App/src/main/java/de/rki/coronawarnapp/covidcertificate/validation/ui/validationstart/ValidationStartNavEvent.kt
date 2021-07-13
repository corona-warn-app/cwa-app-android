package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation

sealed class StartValidationNavEvent

object NavigateToPrivacyFragment : StartValidationNavEvent()
object NavigateToValidationInfoFragment : StartValidationNavEvent()
object ShowNoInternetDialog : StartValidationNavEvent()
data class ShowTimeMessage(val invalidTime: Boolean) : StartValidationNavEvent()
data class ShowErrorDialog(val error: Throwable) : StartValidationNavEvent()
data class NavigateToValidationResultFragment(val validationResult: DccValidation) : StartValidationNavEvent()
