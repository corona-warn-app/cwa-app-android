package de.rki.coronawarnapp.ui.submission

sealed class EuropeanConsentEvent {
    object NavigateToTargetGermany : EuropeanConsentEvent()
    object NavigateToPreviousScreen : EuropeanConsentEvent()
    object NavigateToKeysSubmission : EuropeanConsentEvent()
}
