package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
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
}

data class ExposureResult(
    val exposureWindows: List<ExposureWindow>,
    val aggregatedRiskResult: AggregatedRiskResult?
)
