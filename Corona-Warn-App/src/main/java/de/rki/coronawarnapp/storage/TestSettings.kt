package de.rki.coronawarnapp.storage

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.datastore.tryUpdateValue
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSettings @Inject constructor(
    @StorageDataStore private val dataStore: DataStore<Preferences>,
    @BaseJackson private val objectMapper: ObjectMapper
) {

    val fakeCorrectDeviceTime = dataStore.dataRecovering.distinctUntilChanged(
        key = FAKE_CORRECT_DEVICE_TIME,
        defaultValue = false
    )

    suspend fun updateFakeCorrectDeviceTime(update: (Boolean) -> Boolean) = dataStore.tryUpdateValue(
        preferencesKey = FAKE_CORRECT_DEVICE_TIME,
        transform = { update(it ?: fakeCorrectDeviceTime.first()) }
    )

    val fakeMeteredConnection = dataStore.dataRecovering.distinctUntilChanged(
        key = FAKE_METERED_CONNECTION,
        defaultValue = false
    )

    suspend fun updateFakeMeteredConnection(update: (Boolean) -> Boolean) = dataStore.tryUpdateValue(
        preferencesKey = FAKE_METERED_CONNECTION,
        transform = { update(it ?: fakeMeteredConnection.first()) }
    )

    val fakeExposureWindows = dataStore.dataRecovering
        .distinctUntilChanged(FAKE_EXPOSURE_WINDOWS_TYPE)
        .map { it.toFakeExposureWindowTypes() }

    suspend fun updateFakeExposureWindows(type: FakeExposureWindowTypes) = dataStore.trySetValue(
        preferencesKey = FAKE_EXPOSURE_WINDOWS_TYPE,
        value = runCatching { objectMapper.writeValueAsString(type) }
            .onFailure { Timber.e(it, "Failed to update fake exposure windows type") }
            .getOrDefault(FakeExposureWindowTypes.DISABLED.name)
    )

    val skipSafetyNetTimeCheck = dataStore.dataRecovering.distinctUntilChanged(
        key = SKIP_SAFETYNET_TIME_CHECK,
        defaultValue = false
    )

    suspend fun updateSkipSafetyNetTimeCheck(update: (Boolean) -> Boolean) = dataStore.tryUpdateValue(
        preferencesKey = SKIP_SAFETYNET_TIME_CHECK,
        transform = { update(it ?: skipSafetyNetTimeCheck.first()) }
    )

    private fun String?.toFakeExposureWindowTypes(): FakeExposureWindowTypes = runCatching {
        this?.let { objectMapper.readValue<FakeExposureWindowTypes>(it) }
    }
        .onFailure { Timber.e(it, "Failed to convert %s to FakeExposureWindowTypes", this) }
        .getOrNull() ?: FakeExposureWindowTypes.DISABLED

    enum class FakeExposureWindowTypes {
        @JsonProperty("DISABLED")
        DISABLED,

        @JsonProperty("INCREASED_RISK_DEFAULT")
        INCREASED_RISK_DEFAULT,

        @JsonProperty("INCREASED_RISK_DUE_LOW_RISK_ENCOUNTER_DEFAULT")
        INCREASED_RISK_DUE_LOW_RISK_ENCOUNTER_DEFAULT,

        @JsonProperty("LOW_RISK_DEFAULT")
        LOW_RISK_DEFAULT
    }

    companion object {
        @VisibleForTesting
        val FAKE_CORRECT_DEVICE_TIME = booleanPreferencesKey("config.devicetimecheck.fake.correct")

        @VisibleForTesting
        val FAKE_METERED_CONNECTION = booleanPreferencesKey("connections.metered.fake")

        @VisibleForTesting
        val FAKE_EXPOSURE_WINDOWS_TYPE = stringPreferencesKey("riskleve.exposurewindows.fake")

        @VisibleForTesting
        val SKIP_SAFETYNET_TIME_CHECK = booleanPreferencesKey("safetynet.skip.timecheck")
    }
}
