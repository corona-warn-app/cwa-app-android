package de.rki.coronawarnapp.ui.submission

import de.rki.coronawarnapp.submission.StartOfSymptoms

sealed class SymptomCalendarEvent {
    object NavigateToNext : SymptomCalendarEvent()
    object NavigateToPrevious : SymptomCalendarEvent()
    object SelectLastSeven : SymptomCalendarEvent()
    object SelectOneToTwoWeeks : SymptomCalendarEvent()
    object SelectMoreThanTwoWeeks : SymptomCalendarEvent()
    object SelectNoInformation : SymptomCalendarEvent()
}
