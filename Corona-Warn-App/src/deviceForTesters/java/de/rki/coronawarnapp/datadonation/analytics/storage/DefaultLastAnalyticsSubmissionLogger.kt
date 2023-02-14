package de.rki.coronawarnapp.datadonation.analytics.storage

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.coronawarnapp.util.serialization.readValue
import de.rki.coronawarnapp.util.serialization.writeValue
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class DefaultLastAnalyticsSubmissionLogger @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
    @BaseJackson private val mapper: ObjectMapper,
    private val timeStamper: TimeStamper
) : LastAnalyticsSubmissionLogger {
    private val analyticsDir = File(context.cacheDir, "analytics_storage")
    private val analyticsFile = File(analyticsDir, "last_analytics.bin")

    override suspend fun storeAnalyticsData(analyticsProto: PpaData.PPADataAndroid) =
        withContext(dispatcherProvider.IO) {
            if (!analyticsDir.exists()) {
                analyticsDir.mkdirs()
            }

            val dataObject = LastAnalyticsSubmission(
                timestamp = timeStamper.nowUTC,
                ppaDataAndroid = analyticsProto
            )

            try {
                mapper.writeValue(dataObject, analyticsFile)
            } catch (e: Exception) {
                Timber.e(e, "Failed to store analytics data.")
            }
        }

    override suspend fun getLastAnalyticsData(): LastAnalyticsSubmission? = withContext(dispatcherProvider.IO) {
        try {
            mapper.readValue<LastAnalyticsSubmission>(analyticsFile)?.also {
                requireNotNull(it.ppaDataAndroid)
                requireNotNull(it.timestamp)
            }
        } catch (e: Exception) {
            Timber.e(e, "Couldn't load analytics data.")
            null
        }
    }
}
