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
class DownloadDiagnosisKeysSettings @Inject constructor(
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

    /**
     * true if, and only if, since last runtime the app was updated from 1.7 (or earlier) to a version >= 1.8.0
     */
    val isUpdateToEnfV2: Boolean
        get() = lastVersionCode < VERSION_CODE_FIRST_POSSIBLE_1_8_RELEASE

    var lastVersionCode: Int
        get() = prefs.getInt(KEY_LAST_VERSION_CODE, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_VERSION_CODE, value).apply()

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

    companion object {
        private const val KEY_LAST_VERSION_CODE = "download.task.last.versionCode"
        private const val VERSION_CODE_FIRST_POSSIBLE_1_8_RELEASE = 1080000
    }
}
