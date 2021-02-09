package de.rki.coronawarnapp.datadonation.analytics.storage

import android.content.Context
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DefaultLastAnalyticsSubmissionLogger @Inject constructor(
    @AppContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : LastAnalyticsSubmissionLogger {
    private val analyticsDir = File(context.cacheDir, "analytics_storage")
    private val analyticsFile = File(analyticsDir, "analytics.bin")

    override suspend fun storeAnalyticsData(analyticsProto: PpaData.PPADataAndroid) =
        withContext(dispatcherProvider.IO) {
            if (!analyticsDir.exists()) {
                analyticsDir.mkdirs()
            }

            analyticsFile.writeBytes(analyticsProto.toByteArray())
        }

    override suspend fun getLastAnalyticsData(): PpaData.PPADataAndroid? = withContext(dispatcherProvider.IO) {
        try {
            analyticsFile.readBytes().let {
                PpaData.PPADataAndroid.parseFrom(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}
