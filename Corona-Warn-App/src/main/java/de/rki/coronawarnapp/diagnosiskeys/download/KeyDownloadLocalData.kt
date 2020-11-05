package de.rki.coronawarnapp.diagnosiskeys.download

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyDownloadLocalData @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) {

    private val prefs by lazy {
        context.getSharedPreferences("keydownload_localdata", Context.MODE_PRIVATE)
    }

    val lastDownload = FlowPreference(
        preferences = prefs,
        key = "download.last",
        reader = FlowPreference.createGsonReader<LastDownload?>(gson, null),
        writer = FlowPreference.createGsonWriter(gson)
    )

    data class LastDownload(
        val startedAt: Instant,
        val finishedAt: Instant? = null,
        val successful: Boolean = false
    )
}
