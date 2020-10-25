package de.rki.coronawarnapp.nearby.modules.calculationtracker

import android.content.Context
import com.google.gson.GsonBuilder
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.gson.fromJson
import de.rki.coronawarnapp.util.gson.toJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculationTrackerStorage @Inject constructor(
    @AppContext private val context: Context
) {
    private val mutex = Mutex()
    private val storageDir = File(context.filesDir, "calcuation_tracker")
    private val storageFile = File(storageDir, "calculations.json")
    private var lastCalcuationData: Map<String, Calculation>? = null

    private val gson by lazy {
        if (storageDir.mkdirs()) {
            Timber.v("Created %s", storageDir)
        }
        GsonBuilder().create()
    }

    init {
        Timber.v("init()")
    }

    suspend fun load(): Map<String, Calculation> = mutex.withLock {
        return@withLock try {
            if (!storageFile.exists()) return@withLock emptyMap()

            gson.fromJson<Map<String, Calculation>>(storageFile).also {
                Timber.v("Loaded calculation data: %s", it)
                lastCalcuationData = it
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load tracked calculations.")
            emptyMap()
        }
    }

    suspend fun save(data: Map<String, Calculation>) = mutex.withLock {
        if (lastCalcuationData == data) {
            Timber.v("Data didn't change, skipping save.")
            return@withLock
        }
        Timber.v("Storing calculation data: %s", data)
        try {
            gson.toJson(data, storageFile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save tracked calculations.")
        }
    }
}
