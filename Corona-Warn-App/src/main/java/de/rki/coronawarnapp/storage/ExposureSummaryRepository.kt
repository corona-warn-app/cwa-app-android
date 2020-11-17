package de.rki.coronawarnapp.storage

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.RiskLevels
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class ExposureSummaryRepository @Inject constructor(
    private val riskLevels: RiskLevels
) {

    companion object {
        private val internalMatchedKeyCount = MutableStateFlow(0)
        val matchedKeyCount: Flow<Int> = internalMatchedKeyCount

        private val internalDaysSinceLastExposure = MutableStateFlow(0)
        val daysSinceLastExposure: Flow<Int> = internalDaysSinceLastExposure
    }

    private var entities = emptyList<ExposureWindow>()

    var exposureWindowEntities: List<ExposureWindow>
        get() = entities
        set(value) {
            entities = value
            internalMatchedKeyCount.value = riskLevels.matchedKeyCount(value)
            internalDaysSinceLastExposure.value = riskLevels.daysSinceLastExposure(value)
        }
}
