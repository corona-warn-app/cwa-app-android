package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.repository.ExposureResultRepository
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureResultStore @Inject constructor(
    private val repository: ExposureResultRepository
) {

    val entities = MutableStateFlow(repository.load() ?: emptyResult).onEach { repository.upsert(it) }

    internal val internalMatchedKeyCount = MutableStateFlow(0)
    val matchedKeyCount: Flow<Int> = internalMatchedKeyCount

    internal val internalDaysSinceLastExposure = MutableStateFlow(0)
    val daysSinceLastExposure: Flow<Int> = internalDaysSinceLastExposure

    companion object {
        val emptyResult = ExposureResult(
            exposureWindows = emptyList(),
            aggregatedRiskResult = null
        )
    }
}

data class ExposureResult(
    val exposureWindows: List<ExposureWindow>,
    val aggregatedRiskResult: AggregatedRiskResult?
)
