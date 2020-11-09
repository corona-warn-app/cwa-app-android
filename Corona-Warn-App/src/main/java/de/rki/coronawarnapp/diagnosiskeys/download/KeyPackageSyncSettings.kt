package de.rki.coronawarnapp.diagnosiskeys.download

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.createFlowPreference
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

    data class LastDownload(
        val startedAt: Instant,
        val finishedAt: Instant? = null,
        val successful: Boolean = false,
        val newData: Boolean = false
    )

    val allowMeteredConnections = prefs.createFlowPreference(
        key = "download.connections.metered.allow",
        defaultValue = false
    )
}
