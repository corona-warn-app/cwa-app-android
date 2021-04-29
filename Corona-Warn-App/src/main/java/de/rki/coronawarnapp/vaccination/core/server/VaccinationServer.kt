package de.rki.coronawarnapp.vaccination.core.server

import java.util.Locale

/**
 * Talks with CWA servers
 */
class VaccinationServer {
    suspend fun getVaccinationValueSets(languageCode: Locale): VaccinationValueSet {
        throw NotImplementedError()
    }

    fun clear() {
        // Clear cache
        throw NotImplementedError()
    }
}
