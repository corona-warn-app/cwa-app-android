package de.rki.coronawarnapp.ui.eventregistration.organizer

import de.rki.coronawarnapp.ui.eventregistration.TraceLocationPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceLocationOrganizerSettings @Inject constructor(val preferences: TraceLocationPreferences) {

    var qrInfoAcknowledged: Boolean
        get() {
            return preferences.qrInfoAcknowledged.value
        }
        set(value) = preferences.qrInfoAcknowledged.update { value }
}
