package de.rki.coronawarnapp.bugreporting.settings

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.UploadHistory
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseGson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BugReportingSettings @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) : Resettable {

    private val prefs by lazy {
        context.getSharedPreferences("bugreporting_localdata", Context.MODE_PRIVATE)
    }

    val uploadHistory: FlowPreference<UploadHistory> = prefs.createFlowPreference(
        key = "upload.history",
        reader = FlowPreference.gsonReader(gson, UploadHistory()),
        writer = FlowPreference.gsonWriter(gson)
    )

    override suspend fun reset() {
        Timber.d("reset()")
        prefs.clearAndNotify()
    }
}
