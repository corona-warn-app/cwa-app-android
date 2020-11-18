package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class ExposureResultStore @Inject constructor() {

    val entities = MutableStateFlow<ExposureResult>(Pair(emptyList(), null))
}

typealias ExposureResult = Pair<List<ExposureWindow>, AggregatedRiskResult?>
