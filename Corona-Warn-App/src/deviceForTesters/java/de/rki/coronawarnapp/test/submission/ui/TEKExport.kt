package de.rki.coronawarnapp.test.submission.ui

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage

data class TEKExport(
    val exportText: String
)

fun List<TEKHistoryStorage.TEKBatch>.toExportedKeys() = this
    .sortedBy { it.obtainedAt }
    .flatMap { batch ->
        batch.keys.map { keyInbatch ->
            keyInbatch.toExportedTEK(batch)
        }
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

fun TemporaryExposureKey.toExportedTEK(tekBatch: TEKHistoryStorage.TEKBatch) = ExportedTEK(
    obtainedAt = tekBatch.obtainedAt.toString(),
    keyData = this.keyData,
    rollingStartIntervalNumber = this.rollingStartIntervalNumber,
    transmissionRiskLevel = this.transmissionRiskLevel,
    rollingPeriod = this.rollingPeriod,
    reportType = this.reportType,
    daysSinceOnsetOfSymptoms = this.daysSinceOnsetOfSymptoms
)
