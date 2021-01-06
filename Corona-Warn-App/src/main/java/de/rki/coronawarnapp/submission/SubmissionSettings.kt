package de.rki.coronawarnapp.submission

import android.content.Context
import com.google.gson.Gson
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

    val hasGivenConsent = prefs.createFlowPreference(
        key = "key_submission_consent",
        defaultValue = false
    )

    val hasViewedTestResult = prefs.createFlowPreference(
        key = "key_submission_result_viewed",
        defaultValue = false
    )

    val symptoms: FlowPreference<Symptoms?> = FlowPreference(
        prefs,
        key = "submission.symptoms.latest",
        reader = FlowPreference.gsonReader<Symptoms?>(gson, null),
        writer = FlowPreference.gsonWriter(gson)
    )

    val lastSubmissionUserActivityUTC = prefs.createFlowPreference(
        key = "submission.user.activity.last",
        reader = { key ->
            Instant.ofEpochMilli(getLong(key, 0L))
        },
        writer = { key, value ->
            putLong(key, value.millis)
        }
    )

    val autoSubmissionEnabled = prefs.createFlowPreference(
        key = "submission.auto.enabled",
        defaultValue = false
    )

    val autoSubmissionAttemptsCount = prefs.createFlowPreference(
        key = "submission.auto.attempts.count",
        defaultValue = 0
    )

    val autoSubmissionAttemptsLast = prefs.createFlowPreference(
        key = "submission.auto.attempts.last",
        reader = { key ->
            Instant.ofEpochMilli(getLong(key, 0L))
        },
        writer = { key, value ->
            putLong(key, value.millis)
        }
    )

    fun clear() {
        prefs.clearAndNotify()
    }
}
