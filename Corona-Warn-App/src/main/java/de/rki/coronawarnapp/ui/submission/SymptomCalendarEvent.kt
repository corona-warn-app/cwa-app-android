package de.rki.coronawarnapp.ui.submission

sealed class SymptomCalendarEvent {
    object NavigateToNext : SymptomCalendarEvent()
    object NavigateToPrevious : SymptomCalendarEvent()
    object SelectLastSeven : SymptomCalendarEvent()
    object SelectOneToTwoWeeks : SymptomCalendarEvent()
    object SelectMoreThanTwoWeeks : SymptomCalendarEvent()
    object SelectNoInformation : SymptomCalendarEvent()
}
