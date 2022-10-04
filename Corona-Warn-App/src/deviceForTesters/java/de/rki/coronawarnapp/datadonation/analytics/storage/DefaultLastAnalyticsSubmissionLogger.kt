package de.rki.coronawarnapp.datadonation.analytics.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.util.serialization.toJson
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class DefaultLastAnalyticsSubmissionLogger @Inject constructor(
    @AppContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
    @BaseGson private val baseGson: Gson,
    private val timeStamper: TimeStamper
) : LastAnalyticsSubmissionLogger {
    private val analyticsDir = File(context.cacheDir, "analytics_storage")
    private val analyticsFile = File(analyticsDir, "last_analytics.bin")

    private val gson by lazy {
        baseGson.newBuilder()
            .registerTypeAdapter(PpaData.PPADataAndroid::class.java, PPADataAndroidAdapter())
            .create()
    }

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
                gson.toJson(dataObject, analyticsFile)
            } catch (e: Exception) {
                Timber.e(e, "Failed to store analytics data.")
            }
        }

    override suspend fun getLastAnalyticsData(): LastAnalyticsSubmission? = withContext(dispatcherProvider.IO) {
        try {
            gson.fromJson<LastAnalyticsSubmission>(analyticsFile)?.also {
                requireNotNull(it.ppaDataAndroid)
                requireNotNull(it.timestamp)
            }
        } catch (e: Exception) {
            Timber.e(e, "Couldn't load analytics data.")
            null
        }
    }

    companion object {
        class PPADataAndroidAdapter : TypeAdapter<PpaData.PPADataAndroid>() {
            override fun write(out: JsonWriter, value: PpaData.PPADataAndroid?) {
                if (value == null) out.nullValue()
                else value.toByteArray()?.toByteString()?.base64().let { out.value(it) }
            }

            override fun read(reader: JsonReader): PpaData.PPADataAndroid? = when (reader.peek()) {
                JsonToken.NULL -> reader.nextNull().let { null }
                else -> {
                    val raw = reader.nextString().decodeBase64()?.toByteArray()
                    if (raw == null) {
                        throw JsonParseException("Can't decode base64 ByteArray")
                    } else {
                        PpaData.PPADataAndroid.parseFrom(raw)
                    }
                }
            }
        }
    }
}
