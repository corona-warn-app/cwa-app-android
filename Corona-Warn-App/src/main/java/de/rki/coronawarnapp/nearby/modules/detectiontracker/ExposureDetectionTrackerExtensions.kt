package de.rki.coronawarnapp.nearby.modules.detectiontracker

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

suspend fun ExposureDetectionTracker.lastSubmission(
    onlyFinished: Boolean = true
): TrackedExposureDetection? = calculations
    .first().values
    .filter { it.isSuccessful || !onlyFinished }
    .maxByOrNull { it.startedAt }

fun ExposureDetectionTracker.latestSubmission(
    onlySuccessful: Boolean = true
): Flow<TrackedExposureDetection?> = calculations
    .map { entries ->
        entries.values.filter { it.isSuccessful || !onlySuccessful }
    }
    .map { detections ->
        detections.maxByOrNull { it.startedAt }
    }
