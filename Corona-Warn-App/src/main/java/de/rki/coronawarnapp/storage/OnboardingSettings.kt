package de.rki.coronawarnapp.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.map
import de.rki.coronawarnapp.util.datastore.trySetValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingSettings @Inject constructor(
    @StorageDataStore private val dataStore: DataStore<Preferences>
) {

    val onboardingCompletedTimestamp = dataStore.dataRecovering
        .map(ONBOARDING_COMPLETED_TIMESTAMP, 0L)
        .map { if (it != 0L) Instant.ofEpochMilli(it) else null }
        .distinctUntilChanged()

    suspend fun updateOnboardingCompletedTimestamp(timeStamp: Instant?) = dataStore.trySetValue(
        preferencesKey = ONBOARDING_COMPLETED_TIMESTAMP,
        value = timeStamp?.toEpochMilli() ?: 0L
    )

    val isOnboardedFlow: Flow<Boolean> = onboardingCompletedTimestamp.map { it != null }

    suspend fun isOnboarded() = isOnboardedFlow.first()

    val fabScannerOnboardingDone = dataStore.dataRecovering.distinctUntilChanged(
        key = ONBOARDING_FAB_SCANNER_DONE,
        defaultValue = false
    )

    val exportAllOnboardingDone = dataStore.dataRecovering.distinctUntilChanged(
        key = ONBOARDING_EXPORT_ALL_DONE,
        defaultValue = false
    )

    suspend fun updateFabScannerOnboardingDone(isDone: Boolean) = dataStore.trySetValue(
        preferencesKey = ONBOARDING_FAB_SCANNER_DONE,
        value = isDone
    )

    suspend fun updateExportAllOnboardingDone(isDone: Boolean) = dataStore.trySetValue(
        preferencesKey = ONBOARDING_EXPORT_ALL_DONE,
        value = isDone
    )

    val isBackgroundCheckDone = dataStore.dataRecovering.distinctUntilChanged(
        key = BACKGROUND_CHECK_DONE,
        defaultValue = false
    )

    suspend fun updateBackgroundCheckDone(isDone: Boolean) = dataStore.trySetValue(
        preferencesKey = BACKGROUND_CHECK_DONE,
        value = isDone
    )

    companion object {
        private val ONBOARDING_COMPLETED_TIMESTAMP = longPreferencesKey("onboarding.done.timestamp")
        private val BACKGROUND_CHECK_DONE = booleanPreferencesKey("onboarding.background.checked")
        private val ONBOARDING_FAB_SCANNER_DONE = booleanPreferencesKey("onboarding.fab.scanner.done")
        private  val ONBOARDING_EXPORT_ALL_DONE = booleanPreferencesKey("onboarding.dcc.export_all.done")

    }
}
