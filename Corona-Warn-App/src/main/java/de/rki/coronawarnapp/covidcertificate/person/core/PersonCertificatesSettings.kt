package de.rki.coronawarnapp.covidcertificate.person.core

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.mutate
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonCertificatesSettings @Inject constructor(
    @PersonSettingsDataStore private val dataStore: DataStore<Preferences>,
    @BaseJackson private val mapper: ObjectMapper,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) {

    val currentCwaUser: Flow<CertificatePersonIdentifier?> = dataStore.data.map { prefs ->
        val json = prefs[CURRENT_PERSON_KEY]
        try {
            mapper.readValue(json.orEmpty())
        } catch (e: Exception) {
            Timber.tag(TAG).d("currentCwaUser failed to parse %s", json)
            null
        }
    }

    val personsSettings: Flow<Map<CertificatePersonIdentifier, PersonSettings>> = dataStore.data.map { prefs ->
        val json = prefs[PERSONS_SETTINGS_MAP]
        try {
            mapper.readValue(json.orEmpty())
        } catch (e: Exception) {
            Timber.tag(TAG).d("personsSettings failed to parse %s", json)
            emptyMap()
        }
    }

    fun setDccReissuanceNotifiedAt(
        personIdentifier: CertificatePersonIdentifier,
        time: Instant = Instant.now()
    ) = appScope.launch {
        Timber.tag(TAG).d("setDccReissuanceNotifiedAt()")
        personsSettings.first().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(showDccReissuanceBadge = true, lastDccReissuanceNotifiedAt = time)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    fun dismissReissuanceBadge(
        personIdentifier: CertificatePersonIdentifier
    ) = appScope.launch {
        Timber.tag(TAG).d("dismissReissuanceBadge()")
        personsSettings.first().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(showDccReissuanceBadge = false)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    fun setBoosterNotifiedAt(
        personIdentifier: CertificatePersonIdentifier,
        time: Instant = Instant.now()
    ) = appScope.launch {
        Timber.tag(TAG).d("setBoosterNotifiedAt()")
        personsSettings.first().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(lastBoosterNotifiedAt = time)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    fun acknowledgeBoosterRule(
        personIdentifier: CertificatePersonIdentifier,
        boosterIdentifier: String
    ) = appScope.launch {
        Timber.tag(TAG).d("acknowledgeBoosterRule()")
        personsSettings.first().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(lastSeenBoosterRuleIdentifier = boosterIdentifier)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    fun clearBoosterRuleInfo(
        personIdentifier: CertificatePersonIdentifier
    ) = appScope.launch {
        Timber.tag(TAG).d("clearBoosterRuleInfo()")
        personsSettings.first().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(lastSeenBoosterRuleIdentifier = null, lastBoosterNotifiedAt = null)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    fun cleanOutdatedPerson(personIdentifiers: Set<CertificatePersonIdentifier>) = appScope.launch {
        Timber.tag(TAG).d("cleanOutdatedPerson()")
        personsSettings.first().mutate {
            val personsToClean = keys subtract personIdentifiers
            personsToClean.forEach { remove(it) }
            saveSettings(toMap())
        }
    }

    fun clear() = appScope.launch {
        Timber.d("clear()")
        dataStore.edit { preferences -> preferences.clear() }
    }

    fun removeCurrentCwaUser() = appScope.launch {
        Timber.tag(TAG).d("removeCurrentCwaUser()")
        dataStore.edit { prefs -> prefs.remove(CURRENT_PERSON_KEY) }
    }

    fun setCurrentCwaUser(personIdentifier: CertificatePersonIdentifier?) =
        appScope.launch(context = dispatcherProvider.IO) {
            Timber.tag(TAG).d("setCurrentCwaUser()")
            dataStore.edit { prefs ->
                prefs[CURRENT_PERSON_KEY] = runCatching { mapper.writeValueAsString(personIdentifier) }
                    .onFailure { Timber.tag(TAG).d(it, "setCurrentCwaUser failed") }
                    .getOrDefault("")
            }
        }

    private suspend fun saveSettings(map: Map<CertificatePersonIdentifier, PersonSettings>) {
        dataStore.edit { prefs ->
            prefs[PERSONS_SETTINGS_MAP] = runCatching { mapper.writeValueAsString(map) }
                .onFailure { Timber.tag(TAG).d(it, "cleanOutdatedPerson failed") }
                .getOrDefault("")
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
