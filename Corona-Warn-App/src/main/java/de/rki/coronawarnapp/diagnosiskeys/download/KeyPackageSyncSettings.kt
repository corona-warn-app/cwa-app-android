package de.rki.coronawarnapp.diagnosiskeys.download

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.serialization.BaseGson
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyPackageSyncSettings @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) {

    private val prefs by lazy {
        context.getSharedPreferences("keysync_localdata", Context.MODE_PRIVATE)
    }

    val lastDownloadDays = FlowPreference(
        preferences = prefs,
        key = "download.last.days",
        reader = FlowPreference.gsonReader<LastDownload?>(gson, null),
        writer = FlowPreference.gsonWriter(gson)
    )
    val lastDownloadHours = FlowPreference(
        preferences = prefs,
        key = "download.last.hours",
        reader = FlowPreference.gsonReader<LastDownload?>(gson, null),
        writer = FlowPreference.gsonWriter(gson)
    )

    @SuppressLint("ApplySharedPref")
    fun clear() {
        prefs.clearAndNotify()
    }

    data class LastDownload(
        val startedAt: Instant,
        val finishedAt: Instant? = null,
        val successful: Boolean = false,
        val newData: Boolean = false
    )
}
