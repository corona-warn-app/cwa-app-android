package de.rki.coronawarnapp.risk.storage.internal.riskresults

import androidx.room.Embedded
import androidx.room.Relation
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDao

data class PersistedRiskLevelResultDaoWrapper(
    @Embedded
    val riskLevelLevelResultDao: PersistedRiskLevelResultDao,
    @Relation(parentColumn = "id", entityColumn = "riskLevelResultId")
    val exposureWindows: List<PersistedExposureWindowDao>
)
