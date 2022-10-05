package de.rki.coronawarnapp.diagnosiskeys.download

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseGson
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadDiagnosisKeysSettings @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) : Resettable {

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

    var lastVersionCode: Long
        get() = prefs.getLong(KEY_LAST_VERSION_CODE, -1L)
        set(value) = prefs.edit {
            putLong(KEY_LAST_VERSION_CODE, value)
        }

    override suspend fun reset() {
        Timber.d("reset()")
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
        internal const val VERSION_CODE_FIRST_POSSIBLE_1_8_RELEASE = 1080000
    }
}

/**
 * true if, and only if, since last runtime the app was updated from 1.7 (or earlier) to a version >= 1.8.0
 */
val DownloadDiagnosisKeysSettings.isUpdateToEnfV2: Boolean
    get() = lastVersionCode < DownloadDiagnosisKeysSettings.VERSION_CODE_FIRST_POSSIBLE_1_8_RELEASE

fun DownloadDiagnosisKeysSettings.updateLastVersionCodeToCurrent() {
    lastVersionCode = BuildConfigWrap.VERSION_CODE.also {
        Timber.d("lastVersionCode updated to %d", it)
    }
}
