package de.rki.coronawarnapp.ui.submission

sealed class SymptomIntroductionEvent {
    object NavigateToNext : SymptomIntroductionEvent()
    object NavigateToPreviousScreen : SymptomIntroductionEvent()
    object ButtonSelection : SymptomIntroductionEvent()
}
