package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.serialization.SerializationModule.Companion.baseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationStorage @Inject constructor(
    @VaccinationStorageDataStore private val dataStore: DataStore<Preferences>
) {
    private val mutex = Mutex()

    private val gson by lazy {
        baseGson.newBuilder().apply {
            registerTypeAdapterFactory(CwaCovidCertificate.State.typeAdapter)
        }.create()
    }

    suspend fun load(): Set<StoredVaccinationCertificateData> = mutex.withLock {
        Timber.tag(TAG).d("load()")

        return dataStore.dataRecovering.distinctUntilChanged(
            key = PKEY_VACCINATION_CERT, defaultValue = ""
        ).map { value ->
            if (value.isEmpty()) {
                emptySet()
            } else {
                gson.fromJson<Set<StoredVaccinationCertificateData>>(value, TYPE_TOKEN)
            }
        }.first()
    }

    suspend fun save(certificates: Set<StoredVaccinationCertificateData>): Unit = mutex.withLock {
        Timber.tag(TAG).d("save(%s)", certificates.size)

        dataStore.edit {
            if (certificates.isEmpty()) {
                it.remove(PKEY_VACCINATION_CERT)
            } else {
                it[PKEY_VACCINATION_CERT] = gson.toJson(certificates, TYPE_TOKEN)
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
                    gson.fromJson<VaccinatedPersonData>(value as String).also {
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
        val TYPE_TOKEN = object : TypeToken<Set<StoredVaccinationCertificateData>>() {}.type
    }
}
