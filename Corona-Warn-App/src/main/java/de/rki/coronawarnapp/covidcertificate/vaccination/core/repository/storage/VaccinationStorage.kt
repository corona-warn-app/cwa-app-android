package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationStorage @Inject constructor(
    @VaccinationStorageDataStore private val dataStore: DataStore<Preferences>,
    @BaseJackson private val mapper: ObjectMapper,
) {
    private val mutex = Mutex()

    suspend fun load(): Set<StoredVaccinationCertificateData> = mutex.withLock {
        Timber.tag(TAG).d("load()")

        return dataStore.dataRecovering.distinctUntilChanged(
            key = PKEY_VACCINATION_CERT, defaultValue = ""
        ).map { value ->
            if (value.isEmpty()) {
                emptySet()
            } else {
                mapper.readValue<Set<StoredVaccinationCertificateData>>(value)
            }
        }.first()
    }

    suspend fun save(certificates: Set<StoredVaccinationCertificateData>): Unit = mutex.withLock {
        Timber.tag(TAG).d("save(%s)", certificates.size)

        dataStore.edit {
            if (certificates.isEmpty()) {
                it.remove(PKEY_VACCINATION_CERT)
            } else {
                it[PKEY_VACCINATION_CERT] = mapper.writeValueAsString(certificates)
            }
        }
    }

    @Suppress("DEPRECATION")
    suspend fun loadLegacyData(): Set<VaccinatedPersonData> = mutex.withLock {
        Timber.tag(TAG).d("loadLegacyData()")

        val persons = mutableListOf<VaccinatedPersonData>()

        dataStore.data.first().asMap().forEach { (key, value) ->
            if (key.name.startsWith("vaccination.person.")) {
                persons.add(
                    mapper.readValue<VaccinatedPersonData>(value as String).also {
                        Timber.tag(TAG).v("Person loaded: %s", key.name)
                    }
                )
            }
        }

        return persons.toSet()
    }

    suspend fun clearLegacyData(): Unit = mutex.withLock {
        Timber.tag(TAG).d("clearLegacyData()")

        dataStore.edit { pref ->
            pref.asMap().keys.filter { key ->
                key.name.startsWith("vaccination.person.")
            }.forEach { key ->
                Timber.tag(TAG).v("Removing data for %s", key.name)
                pref.remove(key)
            }
        }
    }

    companion object {
        private const val TAG = "VaccinationStorage"
        val PKEY_VACCINATION_CERT = stringPreferencesKey("vaccination.certificate")
    }
}
