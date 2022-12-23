package de.rki.coronawarnapp.nearby.modules.detectiontracker

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.coronawarnapp.util.serialization.toJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureDetectionTrackerStorage @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val baseGson: Gson,
    @BaseJackson private val objectMapper: ObjectMapper,
) {
    private val mutex = Mutex()
    private val storageDir by lazy {
        File(context.filesDir, "calcuation_tracker").apply {
            if (mkdirs()) Timber.v("Created %s", this)
        }
    }
    private val storageFile by lazy { File(storageDir, "calculations.json") }
    private var lastCalculationData: Map<String, TrackedExposureDetection>? = null

    init {
        Timber.v("init()")
    }

    suspend fun load(): Map<String, TrackedExposureDetection> = mutex.withLock {
        loadTrackingData().getOrNull() ?: loadLegacyTrackingData().getOrNull() ?: emptyMap()
    }

    private fun loadTrackingData() = runCatching {
        objectMapper.parseTracking().also {
            lastCalculationData = it
        }
    }.onFailure {
        Timber.d(it, "loadTrackingData() failed to load tracked detections.")
    }

    private fun loadLegacyTrackingData() = runCatching {
        objectMapper.parseTracking()
    }.onFailure {
        if (storageFile.delete()) Timber.w("Storage file was deleted.")
        Timber.d(it, "loadLegacyTrackingData() failed to load tracked detections.")
    }

    private fun ObjectMapper.parseTracking() =
        readValue<Map<String, TrackedExposureDetection>>(storageFile).also {
            require(it.size >= 0)
            Timber.v("Loaded detection data: %s", it)
        }

    suspend fun save(data: Map<String, TrackedExposureDetection>) = mutex.withLock {
        if (lastCalculationData == data) {
            Timber.v("Data didn't change, skipping save.")
            return@withLock
        }
        Timber.v("Storing detection data: %s", data)
        try {
            baseGson.toJson(data, storageFile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save tracked detections.")
            e.report(ExceptionCategory.INTERNAL)
        }
    }
}
