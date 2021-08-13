package de.rki.coronawarnapp.ui.presencetracing.organizer

import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
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
