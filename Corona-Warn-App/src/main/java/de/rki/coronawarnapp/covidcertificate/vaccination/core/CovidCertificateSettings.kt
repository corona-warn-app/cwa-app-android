package de.rki.coronawarnapp.covidcertificate.vaccination.core

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import de.rki.coronawarnapp.covidcertificate.CovidCertificateSettingsDataStore
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.map
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CovidCertificateSettings @Inject constructor(
    @CovidCertificateSettingsDataStore private val dataStore: DataStore<Preferences>
) : Resettable {

    val isOnboarded = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_COVID_CERTIFICATE_IS_ONBOARDED, defaultValue = false
    )

    suspend fun updateIsOnboarded(value: Boolean) = dataStore.trySetValue(
        preferencesKey = PKEY_COVID_CERTIFICATE_IS_ONBOARDED, value = value
    )

    val lastDccStateBackgroundCheck = dataStore.dataRecovering.map(
        PKEY_COVID_CERTIFICATE_LAST_DCC_STATE_BACKGROUND_CHECK
    ).map { if (it != null && it != 0L) Instant.ofEpochMilli(it) else Instant.EPOCH }.distinctUntilChanged()

    suspend fun updateLastDccStateBackgroundCheck(value: Instant) = dataStore.trySetValue(
        preferencesKey = PKEY_COVID_CERTIFICATE_LAST_DCC_STATE_BACKGROUND_CHECK, value = value.toEpochMilli()
    )

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }
}

@VisibleForTesting
val PKEY_COVID_CERTIFICATE_IS_ONBOARDED = booleanPreferencesKey("covid_certificate_onboarded")
@VisibleForTesting
val PKEY_COVID_CERTIFICATE_LAST_DCC_STATE_BACKGROUND_CHECK = longPreferencesKey("dcc.state.lastcheck")
