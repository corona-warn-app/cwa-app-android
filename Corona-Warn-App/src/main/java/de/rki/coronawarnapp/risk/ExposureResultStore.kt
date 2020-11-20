package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureResultStore @Inject constructor() {

    val entities = MutableStateFlow(
        ExposureResult(
            exposureWindows = emptyList(),
            aggregatedRiskResult = null
        )
    )

    internal val internalMatchedKeyCount = MutableStateFlow(0)
    val matchedKeyCount: Flow<Int> = internalMatchedKeyCount

    internal val internalDaysSinceLastExposure = MutableStateFlow(0)
    val daysSinceLastExposure: Flow<Int> = internalDaysSinceLastExposure
}

data class ExposureResult(
    val exposureWindows: List<ExposureWindow>,
    val aggregatedRiskResult: AggregatedRiskResult?
)
