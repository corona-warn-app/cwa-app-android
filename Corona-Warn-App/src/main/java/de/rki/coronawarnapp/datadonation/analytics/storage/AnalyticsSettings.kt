package de.rki.coronawarnapp.datadonation.analytics.storage

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsSettingsDataStore
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.ExposureRiskMetadata
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.map
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import java.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsSettings @Inject constructor(
    @AnalyticsSettingsDataStore private val dataStore: DataStore<Preferences>
) : Resettable {

    val previousExposureRiskMetadata =
        dataStore.dataRecovering.distinctUntilChanged(key = PREVIOUS_EXPOSURE_RISK_METADATA).map { value ->
            value?.decodeBase64()?.toByteArray()?.let {
                try {
                    ExposureRiskMetadata.parseFrom(it)
                } catch (e: Exception) {
                    null
                }
            }
        }

    suspend fun updatePreviousExposureRiskMetadata(data: ExposureRiskMetadata?) = dataStore.edit {
        if (data == null) {
            it.remove(PREVIOUS_EXPOSURE_RISK_METADATA)
        } else {
            it[PREVIOUS_EXPOSURE_RISK_METADATA] = data.toByteArray().toByteString().base64()
        }
    }

    val userInfoAgeGroup =
        dataStore.dataRecovering.distinctUntilChanged(key = PKEY_USERINFO_AGEGROUP, defaultValue = 0).map { value ->
            PpaData.PPAAgeGroup.forNumber(value)
        }

    suspend fun updateUserInfoAgeGroup(value: PpaData.PPAAgeGroup) = dataStore.edit {
        val numberToWrite = when (value) {
            PpaData.PPAAgeGroup.UNRECOGNIZED -> PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED.number
            else -> value.number
        }
        it[PKEY_USERINFO_AGEGROUP] = numberToWrite
    }

    val userInfoFederalState = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_USERINFO_FEDERALSTATE,
        defaultValue = 0
    ).map { value -> PpaData.PPAFederalState.forNumber(value) }

    suspend fun updateUserInfoFederalState(value: PpaData.PPAFederalState) = dataStore.edit {
        val numberToWrite = when (value) {
            PpaData.PPAFederalState.UNRECOGNIZED -> PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED.number
            else -> value.number
        }
        it[PKEY_USERINFO_FEDERALSTATE] = numberToWrite
    }

    val userInfoDistrict = dataStore.dataRecovering.distinctUntilChanged(key = PKEY_USERINFO_DISTRICT, defaultValue = 0)

    suspend fun updateUserInfoDistrict(value: Int) = dataStore.trySetValue(
        preferencesKey = PKEY_USERINFO_DISTRICT, value = value
    )

    val lastSubmittedTimestamp =
        dataStore.dataRecovering.distinctUntilChanged(key = PKEY_LAST_SUBMITTED_TIMESTAMP, defaultValue = 0L)
            .map { value ->
                if (value != 0L) {
                    Instant.ofEpochMilli(value)
                } else null
            }

    suspend fun updateLastSubmittedTimestamp(value: Instant?) = dataStore.trySetValue(
        preferencesKey = PKEY_LAST_SUBMITTED_TIMESTAMP, value = value?.toEpochMilli() ?: 0L
    )

    val analyticsEnabled =
        dataStore.dataRecovering.distinctUntilChanged(key = PKEY_ANALYTICS_ENABLED, defaultValue = false)

    suspend fun updateAnalyticsEnabled(value: Boolean) = dataStore.trySetValue(
        preferencesKey = PKEY_ANALYTICS_ENABLED, value = value
    )

    val lastOnboardingVersionCode =
        dataStore.dataRecovering.distinctUntilChanged(key = PKEY_ONBOARDED_VERSION_CODE, defaultValue = 0L)

    suspend fun updateLastOnboardingVersionCode(value: Long) = dataStore.trySetValue(
        preferencesKey = PKEY_ONBOARDED_VERSION_CODE, value = value
    )

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }

    companion object {
        @VisibleForTesting
        val PREVIOUS_EXPOSURE_RISK_METADATA = stringPreferencesKey("exposurerisk.metadata.previous")
        @VisibleForTesting
        val PKEY_USERINFO_AGEGROUP = intPreferencesKey("userinfo.agegroup")
        @VisibleForTesting
        val PKEY_USERINFO_FEDERALSTATE = intPreferencesKey("userinfo.federalstate")
        @VisibleForTesting
        val PKEY_USERINFO_DISTRICT = intPreferencesKey("userinfo.district")
        private val PKEY_LAST_SUBMITTED_TIMESTAMP = longPreferencesKey("analytics.submission.timestamp")
        private val PKEY_ANALYTICS_ENABLED = booleanPreferencesKey("analytics.enabled")
        private val PKEY_ONBOARDED_VERSION_CODE = longPreferencesKey("analytics.onboarding.versionCode")
    }
}
