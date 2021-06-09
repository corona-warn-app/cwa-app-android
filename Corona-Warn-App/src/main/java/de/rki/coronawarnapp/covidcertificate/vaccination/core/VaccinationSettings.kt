package de.rki.coronawarnapp.covidcertificate.vaccination.core

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationSettings @Inject constructor(val preferences: VaccinationPreferences) {

    var registrationAcknowledged: Boolean
        get() {
            return preferences.registrationAcknowledged.value
        }
        set(value) = preferences.registrationAcknowledged.update { value }
}
