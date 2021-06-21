package de.rki.coronawarnapp.covidcertificate.vaccination.core

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CovidCertificateSettings @Inject constructor(val preferences: CovidCertificatePreferences) {

    var isOnboardingDone: Boolean
        get() {
            return preferences.isOnboarded.value
        }
        set(value) = preferences.isOnboarded.update { value }
}
