package de.rki.coronawarnapp.nearby.modules.detectiontracker

import kotlinx.coroutines.flow.Flow

interface ExposureDetectionTracker {
    val calculations: Flow<Map<String, TrackedExposureDetection>>

    fun trackNewExposureDetection(identifier: String)

    fun finishExposureDetection(identifier: String, result: TrackedExposureDetection.Result)

    fun clear()
}
