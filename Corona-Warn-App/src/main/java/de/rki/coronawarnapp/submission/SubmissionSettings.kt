package de.rki.coronawarnapp.submission

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantOrNull
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.adapter.RuntimeTypeAdapterFactory
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionSettings @Inject constructor(
    @SubmissionSettingsDataStore private val dataStore: DataStore<Preferences>,
    @BaseGson private val baseGson: Gson
) : Resettable {

    private val gson by lazy {
        baseGson.newBuilder().apply {
            val rta = RuntimeTypeAdapterFactory.of(Symptoms.StartOf::class.java)
                .registerSubtype(Symptoms.StartOf.NoInformation::class.java)
                .registerSubtype(Symptoms.StartOf.LastSevenDays::class.java)
                .registerSubtype(Symptoms.StartOf.MoreThanTwoWeeks::class.java)
                .registerSubtype(Symptoms.StartOf.OneToTwoWeeksAgo::class.java)
                .registerSubtype(Symptoms.StartOf.Date::class.java)

            registerTypeAdapterFactory(rta)
        }.create()
    }

    //region Needed for migration ONLY. Use CoronaTestRepository
    val registrationTokenMigration = dataStore.dataRecovering.distinctUntilChanged(
        key = TEST_REGISTRATION_TOKEN
    )

    suspend fun updateRegistrationTokenMigration(value: String?) = dataStore.trySetValue(
        preferencesKey = TEST_REGISTRATION_TOKEN,
        value = value ?: ""
    )

    suspend fun updateInitialTestResultReceivedAtMigration(value: Instant?) = dataStore.trySetValue(
        preferencesKey = TEST_RESULT_RECEIVED_AT,
        value = value?.toEpochMilli() ?: 0L
    )

    val devicePairingSuccessfulAtMigration = dataStore.dataRecovering.distinctUntilChanged(
        key = TEST_PARING_SUCCESSFUL_AT, defaultValue = 0L
    ).map { it.toInstantOrNull() }

    suspend fun updateDevicePairingSuccessfulAtMigration(value: Instant?) = dataStore.trySetValue(
        preferencesKey = TEST_PARING_SUCCESSFUL_AT,
        value = value?.toEpochMilli() ?: 0L
    )

    val isSubmissionSuccessfulMigration = dataStore.dataRecovering.distinctUntilChanged(
        key = IS_KEY_SUBMISSION_SUCCESSFUL,
        defaultValue = false
    )

    suspend fun updateIsSubmissionSuccessfulMigration(value: Boolean) = dataStore.trySetValue(
        preferencesKey = IS_KEY_SUBMISSION_SUCCESSFUL,
        value = value
    )

    val isAllowedToSubmitKeysMigration = dataStore.dataRecovering.distinctUntilChanged(
        key = IS_KEY_SUBMISSION_ALLOWED,
        defaultValue = false
    )

    suspend fun updateIsAllowedToSubmitKeysMigration(value: Boolean) = dataStore.trySetValue(
        preferencesKey = IS_KEY_SUBMISSION_ALLOWED,
        value = value
    )

    val hasGivenConsentMigration = dataStore.dataRecovering.distinctUntilChanged(
        key = SUBMISSION_CONSENT_GIVEN,
        defaultValue = false
    )

    val hasViewedTestResultMigration = dataStore.dataRecovering.distinctUntilChanged(
        key = SUBMISSION_RESULT_VIEWED,
        defaultValue = false
    )
    //endregion Needed for migration ONLY.

    val symptoms: Flow<Symptoms?> = dataStore.dataRecovering.distinctUntilChanged(
        key = SUBMISSION_SYMPTOMS_LATEST, defaultValue = ""
    ).map { value ->
        if (value.isNotEmpty()) gson.fromJson(value) else null
    }

    suspend fun updateSymptoms(value: Symptoms?) = dataStore.trySetValue(
        preferencesKey = SUBMISSION_SYMPTOMS_LATEST,
        value = gson.toJson(value)
    )

    val lastSubmissionUserActivityUTC = dataStore.dataRecovering.distinctUntilChanged(
        key = AUTO_SUBMISSION_LAST_USER_ACTIVITY, defaultValue = 0L
    ).map { Instant.ofEpochMilli(it) }

    suspend fun updateLastSubmissionUserActivityUTC(value: Instant) = dataStore.trySetValue(
        preferencesKey = AUTO_SUBMISSION_LAST_USER_ACTIVITY,
        value = value.toEpochMilli()
    )

    val autoSubmissionEnabled = dataStore.dataRecovering.distinctUntilChanged(
        key = AUTO_SUBMISSION_ENABLED,
        defaultValue = false
    )

    suspend fun updateAutoSubmissionEnabled(value: Boolean) = dataStore.trySetValue(
        preferencesKey = AUTO_SUBMISSION_ENABLED,
        value = value
    )

    val autoSubmissionAttemptsCount = dataStore.dataRecovering.distinctUntilChanged(
        key = AUTO_SUBMISSION_ATTEMPT_COUNT,
        defaultValue = 0
    )

    suspend fun updateAutoSubmissionAttemptsCount(value: Int) = dataStore.trySetValue(
        preferencesKey = AUTO_SUBMISSION_ATTEMPT_COUNT,
        value = value
    )

    val autoSubmissionAttemptsLast = dataStore.dataRecovering.distinctUntilChanged(
        key = AUTO_SUBMISSION_LAST_ATTEMPT, defaultValue = 0L
    ).map { Instant.ofEpochMilli(it) }

    suspend fun updateAutoSubmissionAttemptsLast(value: Instant) = dataStore.trySetValue(
        preferencesKey = AUTO_SUBMISSION_LAST_ATTEMPT,
        value = value.toEpochMilli()
    )

    suspend fun deleteLegacyTestData() {
        Timber.d("deleteLegacyTestData()")
        dataStore.edit { prefs ->
            prefs.remove(SUBMISSION_RESULT_VIEWED)
            prefs.remove(TEST_REGISTRATION_TOKEN)
            prefs.remove(TEST_PARING_SUCCESSFUL_AT)
            prefs.remove(TEST_RESULT_RECEIVED_AT)
            prefs.remove(IS_KEY_SUBMISSION_ALLOWED)
            prefs.remove(IS_KEY_SUBMISSION_SUCCESSFUL)
            prefs.remove(SUBMISSION_CONSENT_GIVEN)
        }
    }

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }

    companion object {
        private val TEST_REGISTRATION_TOKEN = stringPreferencesKey("submission.test.token")
        private val TEST_RESULT_RECEIVED_AT = longPreferencesKey("submission.test.result.receivedAt")
        private val TEST_PARING_SUCCESSFUL_AT = longPreferencesKey("submission.test.pairedAt")
        private val IS_KEY_SUBMISSION_ALLOWED = booleanPreferencesKey("submission.allowed")
        private val IS_KEY_SUBMISSION_SUCCESSFUL = booleanPreferencesKey("submission.successful")
        private val SUBMISSION_CONSENT_GIVEN = booleanPreferencesKey("key_submission_consent")
        private val SUBMISSION_RESULT_VIEWED = booleanPreferencesKey("key_submission_result_viewed")
        val SUBMISSION_SYMPTOMS_LATEST = stringPreferencesKey("submission.symptoms.latest")
        private val AUTO_SUBMISSION_LAST_USER_ACTIVITY = longPreferencesKey("submission.user.activity.last")
        private val AUTO_SUBMISSION_ENABLED = booleanPreferencesKey("submission.auto.enabled")
        private val AUTO_SUBMISSION_ATTEMPT_COUNT = intPreferencesKey("submission.auto.attempts.count")
        private val AUTO_SUBMISSION_LAST_ATTEMPT = longPreferencesKey("submission.auto.attempts.last")
    }
}
