package de.rki.coronawarnapp.nearby.modules.detectiontracker

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.util.serialization.getDefaultGsonTypeAdapter
import de.rki.coronawarnapp.util.serialization.toJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureDetectionTrackerStorage @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson gson: Gson
) {
    private val gson by lazy {
        gson.newBuilder()
            .registerTypeAdapter(Instant::class.java, Instant::class.getDefaultGsonTypeAdapter())
            .create()
    }

    private val mutex = Mutex()
    private val storageDir by lazy {
        File(context.filesDir, "calcuation_tracker").apply {
            if (mkdirs()) Timber.v("Created %s", this)
        }
    }
    private val storageFile by lazy { File(storageDir, "calculations.json") }
    private var lastCalcuationData: Map<String, TrackedExposureDetection>? = null

    init {
        Timber.v("init()")
    }

    suspend fun load(): Map<String, TrackedExposureDetection> = mutex.withLock {
        return@withLock try {
            gson.fromJson<Map<String, TrackedExposureDetection>>(storageFile)?.also {
                require(it.size >= 0)
                Timber.v("Loaded detection data: %s", it)
                lastCalcuationData = it
            } ?: emptyMap()
        } catch (e: Exception) {
            Timber.e(e, "Failed to load tracked detections.")
            if (storageFile.delete()) Timber.w("Storage file was deleted.")
            emptyMap()
        }
    }

    suspend fun save(data: Map<String, TrackedExposureDetection>) = mutex.withLock {
        if (lastCalcuationData == data) {
            Timber.v("Data didn't change, skipping save.")
            return@withLock
        }
        Timber.v("Storing detection data: %s", data)
        try {
            gson.toJson(data, storageFile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save tracked detections.")
            e.report(ExceptionCategory.INTERNAL)
        }
    }
}
