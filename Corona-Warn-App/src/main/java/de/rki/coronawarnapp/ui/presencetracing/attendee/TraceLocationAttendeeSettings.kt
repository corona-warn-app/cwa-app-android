package de.rki.coronawarnapp.ui.presencetracing.attendee

import dagger.Reusable
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import javax.inject.Inject

@Reusable
class TraceLocationAttendeeSettings @Inject constructor(private val preferences: TraceLocationPreferences) {

    val createJournalEntryCheckedState = preferences.createJournalEntryCheckedState

    suspend fun setCreateJournalEntryCheckedState(isChecked: Boolean) =
        preferences.updateCreateJournalEntryCheckedState(isChecked)
}
