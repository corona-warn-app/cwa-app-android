package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindow
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseGson
import timber.log.Timber
import javax.inject.Inject

class AnalyticsExposureWindowsSettings @Inject constructor(
    @AppContext context: Context,
    @BaseGson val gson: Gson,
): Resettable {
    private val prefs by lazy {
        context.getSharedPreferences("analytics_exposureWindows", Context.MODE_PRIVATE)
    }

    val currentExposureWindows: FlowPreference<List<AnalyticsExposureWindow>?> = prefs.createFlowPreference(
        key = PREFS_KEY_CURRENT_EXPOSURE_WINDOWS,
        reader = FlowPreference.gsonReader<List<AnalyticsExposureWindow>?>(gson, null),
        writer = FlowPreference.gsonWriter(gson)
    )

    override suspend fun reset() {
        Timber.d("reset()")
        prefs.clearAndNotify()
    }
}

private const val PREFS_KEY_CURRENT_EXPOSURE_WINDOWS = "analytics_currentExposureWindows"
