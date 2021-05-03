package de.rki.coronawarnapp.vaccination.core.server

import dagger.Reusable
import java.util.Locale
import javax.inject.Inject

/**
 * Talks with CWA servers
 */
@Reusable
class VaccinationServer @Inject constructor() {
    suspend fun getVaccinationValueSets(languageCode: Locale): VaccinationValueSet {
        throw NotImplementedError()
    }

    fun clear() {
        // Clear cache
        throw NotImplementedError()
    }
}
