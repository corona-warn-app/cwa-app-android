package de.rki.coronawarnapp.ui.submission

sealed class SymptomIntroductionEvent {
    object NavigateToSymptomCalendar : SymptomIntroductionEvent()
    object NavigateToPreviousScreen : SymptomIntroductionEvent()
    object SelectPositive : SymptomIntroductionEvent()

}
