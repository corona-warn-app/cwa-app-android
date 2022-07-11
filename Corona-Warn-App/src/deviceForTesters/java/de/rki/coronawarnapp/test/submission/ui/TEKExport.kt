package de.rki.coronawarnapp.test.submission.ui

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import java.time.Instant

data class TEKExport(
    val exportText: String
)

fun List<TEKHistoryItem>.toExportedKeys() = this
    .sortedBy { it.obtainedAt }
    .map { item ->
        item.key.toExportedTEK(item.obtainedAt)
    }

data class ExportedTEK(
    val obtainedAt: String,
    val keyData: ByteArray,
    val rollingStartIntervalNumber: Int,
    val transmissionRiskLevel: Int,
    val rollingPeriod: Int,
    val reportType: Int,
    val daysSinceOnsetOfSymptoms: Int
)

fun TemporaryExposureKey.toExportedTEK(obtainedAt: Instant) = ExportedTEK(
    obtainedAt = obtainedAt.toString(),
    keyData = this.keyData,
    rollingStartIntervalNumber = this.rollingStartIntervalNumber,
    transmissionRiskLevel = this.transmissionRiskLevel,
    rollingPeriod = this.rollingPeriod,
    reportType = this.reportType,
    daysSinceOnsetOfSymptoms = this.daysSinceOnsetOfSymptoms
)
