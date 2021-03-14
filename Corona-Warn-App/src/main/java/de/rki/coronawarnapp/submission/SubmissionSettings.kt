package de.rki.coronawarnapp.submission

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantOrNull
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.adapter.RuntimeTypeAdapterFactory
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionSettings @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val baseGson: Gson
) {

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

    private val prefs by lazy {
        context.getSharedPreferences("submission_localdata", Context.MODE_PRIVATE)
    }

    val registrationToken = prefs.createFlowPreference<String?>(
        key = TEST_REGISTRATION_TOKEN,
        defaultValue = null
    )

    var initialTestResultReceivedAt: Instant?
        get() = prefs.getLong(TEST_RESULT_RECEIVED_AT, 0L).toInstantOrNull()
        set(value) = prefs.edit { putLong(TEST_RESULT_RECEIVED_AT, value?.millis ?: 0L) }

    var devicePairingSuccessfulAt: Instant?
        get() = prefs.getLong(TEST_PARING_SUCCESSFUL_AT, 0L).toInstantOrNull()
        set(value) = prefs.edit { putLong(TEST_PARING_SUCCESSFUL_AT, value?.millis ?: 0L) }

    var isSubmissionSuccessful: Boolean
        get() = prefs.getBoolean(IS_KEY_SUBMISSION_SUCCESSFUL, false)
        set(value) = prefs.edit { putBoolean(IS_KEY_SUBMISSION_SUCCESSFUL, value) }

    var isAllowedToSubmitKeys: Boolean
        get() = prefs.getBoolean(IS_KEY_SUBMISSION_ALLOWED, false)
        set(value) = prefs.edit { putBoolean(IS_KEY_SUBMISSION_ALLOWED, value) }

    val hasGivenConsent = prefs.createFlowPreference(
        key = SUBMISSION_CONSENT_GIVEN,
        defaultValue = false
    )

    val hasViewedTestResult = prefs.createFlowPreference(
        key = SUBMISSION_RESULT_VIEWED,
        defaultValue = false
    )

    val symptoms: FlowPreference<Symptoms?> = FlowPreference(
        prefs,
        key = SUBMISSION_SYMPTOMS_LATEST,
        reader = FlowPreference.gsonReader<Symptoms?>(gson, null),
        writer = FlowPreference.gsonWriter(gson)
    )

    val lastSubmissionUserActivityUTC = prefs.createFlowPreference(
        key = AUTO_SUBMISSION_LAST_USER_ACTIVITY,
        reader = { key ->
            Instant.ofEpochMilli(getLong(key, 0L))
        },
        writer = { key, value ->
            putLong(key, value.millis)
        }
    )

    val autoSubmissionEnabled = prefs.createFlowPreference(
        key = AUTO_SUBMISSION_ENABLED,
        defaultValue = false
    )

    val autoSubmissionAttemptsCount = prefs.createFlowPreference(
        key = AUTO_SUBMISSION_ATTEMPT_COUNT,
        defaultValue = 0
    )

    val autoSubmissionAttemptsLast = prefs.createFlowPreference(
        key = AUTO_SUBMISSION_LAST_ATTEMPT,
        reader = { key ->
            Instant.ofEpochMilli(getLong(key, 0L))
        },
        writer = { key, value ->
            putLong(key, value.millis)
        }
    )

    fun clear() = prefs.clearAndNotify()

    companion object {
        private const val TEST_REGISTRATION_TOKEN = "submission.test.token"
        private const val TEST_RESULT_RECEIVED_AT = "submission.test.result.receivedAt"
        private const val TEST_PARING_SUCCESSFUL_AT = "submission.test.pairedAt"
        private const val IS_KEY_SUBMISSION_ALLOWED = "submission.allowed"
        private const val IS_KEY_SUBMISSION_SUCCESSFUL = "submission.successful"
        private const val SUBMISSION_CONSENT_GIVEN = "key_submission_consent"
        private const val SUBMISSION_RESULT_VIEWED = "key_submission_result_viewed"
        private const val SUBMISSION_SYMPTOMS_LATEST = "submission.symptoms.latest"
        private const val AUTO_SUBMISSION_LAST_USER_ACTIVITY = "submission.user.activity.last"
        private const val AUTO_SUBMISSION_ENABLED = "submission.auto.enabled"
        private const val AUTO_SUBMISSION_ATTEMPT_COUNT = "submission.auto.attempts.count"
        private const val AUTO_SUBMISSION_LAST_ATTEMPT = "submission.auto.attempts.last"
    }
}
