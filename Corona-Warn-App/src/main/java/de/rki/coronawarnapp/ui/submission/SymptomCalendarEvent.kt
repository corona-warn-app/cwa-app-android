package de.rki.coronawarnapp.ui.submission

sealed class SymptomCalendarEvent {
    object NavigateToNext : SymptomCalendarEvent()
    object NavigateToPrevious : SymptomCalendarEvent()
}
