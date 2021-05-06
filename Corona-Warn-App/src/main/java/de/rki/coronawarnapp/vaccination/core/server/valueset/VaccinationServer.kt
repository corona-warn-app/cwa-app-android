package de.rki.coronawarnapp.vaccination.core.server.valueset

import dagger.Reusable
import timber.log.Timber
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
        Timber.d("Clearing cache")
    }
}
