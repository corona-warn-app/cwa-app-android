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
import androidx.datastore.preferences.core.emptyPreferences
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.Instant
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonCertificatesSettings @Inject constructor(
    @PersonSettingsDataStore private val dataStore: DataStore<Preferences>,
    @BaseJackson private val mapper: ObjectMapper,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) {

    private val dataStoreFlow = dataStore.data
        .catch { e ->
            Timber.tag(TAG).e(e, "Failed to read PersonCertificatesSettings")
            if (e is IOException) emit(emptyPreferences()) else throw e
        }

    val currentCwaUser: Flow<CertificatePersonIdentifier?> = dataStoreFlow
        .map { prefs ->
            mapper.readValue<CertificatePersonIdentifier?>(prefs[CURRENT_PERSON_KEY].orEmpty())
        }.catch {
            Timber.tag(TAG).d(it, "currentCwaUser failed to parse")
            emit(null)
        }

    val personsSettings: Flow<Map<CertificatePersonIdentifier, PersonSettings>> = dataStoreFlow
        .map { prefs ->
            mapper.readValue<SettingsMap>(prefs[PERSONS_SETTINGS_MAP].orEmpty()).settings
        }.catch {
            Timber.tag(TAG).d(it, "personsSettings failed to parse")
            emit(emptyMap())
        }

    fun setDccReissuanceNotifiedAt(
        personIdentifier: CertificatePersonIdentifier,
        time: Instant = Instant.now()
    ) = appScope.launch {
        Timber.tag(TAG).d("setDccReissuanceNotifiedAt()")
        settings().mutate {
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
        settings().mutate {
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
        settings().mutate {
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
        settings().mutate {
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
        settings().mutate {
            val personSettings = get(personIdentifier) ?: PersonSettings()
            val badgeSettings = personSettings.copy(lastSeenBoosterRuleIdentifier = null, lastBoosterNotifiedAt = null)
            this[personIdentifier] = badgeSettings
            saveSettings(toMap())
        }
    }

    fun cleanSettingsNotIn(personIdentifiers: Set<CertificatePersonIdentifier>) = appScope.launch {
        Timber.tag(TAG).d("cleanSettingsNotIn()")
        settings().mutate {
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

    private suspend fun settings() = personsSettings.first()

    private suspend fun saveSettings(
        map: Map<CertificatePersonIdentifier, PersonSettings>
    ) = withContext(dispatcherProvider.IO) {
        dataStore.edit { prefs ->
            prefs[PERSONS_SETTINGS_MAP] = runCatching { mapper.writeValueAsString(SettingsMap(map)) }
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
