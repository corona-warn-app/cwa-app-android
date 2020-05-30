package de.rki.coronawarnapp.storage

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exposure_summary",
    indices = [Index("id")]
)
class ExposureSummaryEntity {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var daysSinceLastExposure: Int = 0

    var matchedKeyCount: Int = 0

    var maximumRiskScore: Int = 0

    var summationRiskScore: Int = 0

    var attenuationDurationsInMinutes: List<Int> = arrayListOf()
}
