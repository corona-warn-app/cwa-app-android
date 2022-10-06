package de.rki.coronawarnapp.covidcertificate.person.core

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.model.PersonSettings
import de.rki.coronawarnapp.covidcertificate.person.model.SettingsMap
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.mutate
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonCertificatesSettings @Inject constructor(
    @PersonSettingsDataStore private val dataStore: DataStore<Preferences>,
    @BaseJackson private val mapper: ObjectMapper
) : Resettable {

    private val dataStoreFlow = dataStore.data
        .catch { e ->
            Timber.tag(TAG).e(e, "Failed to read PersonCertificatesSettings")
            if (e is IOException) emit(emptyPreferences()) else throw e
        }

    val currentCwaUser: Flow<CertificatePersonIdentifier?> = dataStoreFlow
        .map { prefs ->
            runCatching {
                mapper.readValue<CertificatePersonIdentifier?>(prefs[CURRENT_PERSON_KEY].orEmpty())
            }.getOrNull()
        }

    val personsSettings: Flow<Map<CertificatePersonIdentifier, PersonSettings>> = dataStoreFlow
        .map { prefs ->
            runCatching {
                mapper.readValue<SettingsMap>(prefs[PERSONS_SETTINGS_MAP].orEmpty()).settings
            }.onFailure {
                Timber.tag(TAG).d("personsSettings failed to parse => %s", it.message)
            }.getOrDefault(emptyMap())
        }

    suspend fun setDccReissuanceNotifiedAt(
        personIdentifier: CertificatePersonIdentifier,
        time: Instant = Instant.now()
    ) {
        Timber.tag(TAG).d("setDccReissuanceNotifiedAt()")
        settings().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(showDccReissuanceBadge = true, lastDccReissuanceNotifiedAt = time)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    suspend fun dismissReissuanceBadge(
        personIdentifier: CertificatePersonIdentifier
    ) {
        Timber.tag(TAG).d("dismissReissuanceBadge()")
        settings().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(showDccReissuanceBadge = false)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    suspend fun setGStatusNotifiedAt(
        personIdentifier: CertificatePersonIdentifier,
        time: Instant = Instant.now()
    ) {
        Timber.tag(TAG).d("setCurrentAdmissionState()")
        settings().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings =
                personSettings.copy(showAdmissionStateChangedBadge = true, lastAdmissionStateNotifiedAt = time)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    suspend fun dismissGStatusBadge(
        personIdentifier: CertificatePersonIdentifier
    ) {
        Timber.tag(TAG).d("dismissGStatusBadge()")
        settings().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(showAdmissionStateChangedBadge = false)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    suspend fun setBoosterNotifiedAt(
        personIdentifier: CertificatePersonIdentifier,
        time: Instant = Instant.now()
    ) {
        Timber.tag(TAG).d("setBoosterNotifiedAt()")
        settings().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(lastBoosterNotifiedAt = time)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    suspend fun acknowledgeBoosterRule(
        personIdentifier: CertificatePersonIdentifier,
        boosterIdentifier: String
    ) {
        Timber.tag(TAG).d("acknowledgeBoosterRule()")
        settings().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(lastSeenBoosterRuleIdentifier = boosterIdentifier)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    suspend fun clearBoosterRuleInfo(
        personIdentifier: CertificatePersonIdentifier
    ) {
        Timber.tag(TAG).d("clearBoosterRuleInfo()")
        settings().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(lastSeenBoosterRuleIdentifier = null, lastBoosterNotifiedAt = null)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    suspend fun cleanSettingsNotIn(personIdentifiers: Set<CertificatePersonIdentifier>) {
        Timber.tag(TAG).d("cleanSettingsNotIn()")
        settings().mutate {
            val personsToClean = keys subtract personIdentifiers
            personsToClean.forEach { remove(it) }
            saveSettings(toMap())
        }
    }

    override suspend fun reset() {
        Timber.tag(TAG).d("reset()")
        dataStore.edit { preferences -> preferences.clear() }
    }

    suspend fun removeCurrentCwaUser() {
        Timber.tag(TAG).d("removeCurrentCwaUser()")
        dataStore.edit { prefs -> prefs.remove(CURRENT_PERSON_KEY) }
    }

    suspend fun setCurrentCwaUser(personIdentifier: CertificatePersonIdentifier?) {
        Timber.tag(TAG).d("setCurrentCwaUser()")
        dataStore.edit { prefs ->
            prefs[CURRENT_PERSON_KEY] = mapper.writeValueAsString(personIdentifier)
        }
    }

    private suspend fun settings() = personsSettings.first()

    private suspend fun saveSettings(
        map: Map<CertificatePersonIdentifier, PersonSettings>
    ) {
        dataStore.edit { prefs ->
            prefs[PERSONS_SETTINGS_MAP] = mapper.writeValueAsString(SettingsMap(map))
        }
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal val CURRENT_PERSON_KEY = stringPreferencesKey("certificate.person.current")

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal val PERSONS_SETTINGS_MAP = stringPreferencesKey("persons.settings.map")

        private val TAG = tag<PersonCertificatesSettings>()
    }
}
