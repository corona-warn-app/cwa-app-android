package de.rki.coronawarnapp.covidcertificate.validation.core.settings

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.covidcertificate.validation.core.CertificateValidationDataStore
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccValidationSettings @Inject constructor(
    @CertificateValidationDataStore private val dataStore: DataStore<Preferences>,
    private val timeStamper: TimeStamper,
) : Resettable {

    val settings = dataStore.dataRecovering.map { prefs ->
        Pair(
            prefs[DCC_VALIDATION_ARRIVAL_COUNTRY] ?: DccCountry.DE,
            prefs[DCC_VALIDATION_ARRIVAL_TIME] ?: timeStamper.nowUTC.toEpochMilli()
        )
    }

    suspend fun updateDccValidationCountry(
        arrivalCountry: String
    ) = with(dataStore) {
        trySetValue(DCC_VALIDATION_ARRIVAL_COUNTRY, arrivalCountry)
    }

    suspend fun updateDccValidationTime(
        arrivalTime: Long
    ) = with(dataStore) {
        trySetValue(DCC_VALIDATION_ARRIVAL_TIME, arrivalTime)
    }

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }

    companion object {
        @VisibleForTesting
        val DCC_VALIDATION_ARRIVAL_TIME = longPreferencesKey("DccValidationSettings.arrival.timestamp")

        @VisibleForTesting
        val DCC_VALIDATION_ARRIVAL_COUNTRY = stringPreferencesKey("DccValidationSettings.arrival.country")
    }
}
