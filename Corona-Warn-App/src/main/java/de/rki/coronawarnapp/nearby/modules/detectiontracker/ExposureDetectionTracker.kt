package de.rki.coronawarnapp.nearby.modules.detectiontracker

import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow

interface ExposureDetectionTracker : Resettable {
    val calculations: Flow<Map<String, TrackedExposureDetection>>

    fun trackNewExposureDetection(identifier: String)

    fun finishExposureDetection(identifier: String? = null, result: TrackedExposureDetection.Result)
}
