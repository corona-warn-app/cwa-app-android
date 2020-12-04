package de.rki.coronawarnapp.submission.data.tekhistory.internal

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.joda.time.Instant

@Entity(tableName = "tek_history")
data class TEKEntryDao(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "batchId") val batchId: String,
    @ColumnInfo(name = "obtainedAt") val obtainedAt: Instant,
    @Embedded val persistedTEK: PersistedTEK
) {

    data class PersistedTEK(
        @ColumnInfo(name = "keyData") val keyData: ByteArray,
        @ColumnInfo(name = "rollingStartIntervalNumber") val rollingStartIntervalNumber: Int,
        @ColumnInfo(name = "transmissionRiskLevel") val transmissionRiskLevel: Int,
        @ColumnInfo(name = "rollingPeriod") val rollingPeriod: Int,
        @ColumnInfo(name = "reportType") val reportType: Int,
        @ColumnInfo(name = "daysSinceOnsetOfSymptoms") val daysSinceOnsetOfSymptoms: Int
    ) {

        fun toTemporaryExposureKey(): TemporaryExposureKey = TemporaryExposureKey.TemporaryExposureKeyBuilder().apply {
            setKeyData(keyData)
            setRollingStartIntervalNumber(rollingStartIntervalNumber)
            setTransmissionRiskLevel(transmissionRiskLevel)
            setRollingPeriod(rollingPeriod)
            setReportType(reportType)
            if (daysSinceOnsetOfSymptoms != TemporaryExposureKey.DAYS_SINCE_ONSET_OF_SYMPTOMS_UNKNOWN) {
                setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
            }
        }.build()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PersistedTEK

            if (!keyData.contentEquals(other.keyData)) return false
            if (rollingStartIntervalNumber != other.rollingStartIntervalNumber) return false
            if (transmissionRiskLevel != other.transmissionRiskLevel) return false
            if (rollingPeriod != other.rollingPeriod) return false
            if (reportType != other.reportType) return false
            if (daysSinceOnsetOfSymptoms != other.daysSinceOnsetOfSymptoms) return false

            return true
        }

        override fun hashCode(): Int {
            var result = keyData.contentHashCode()
            result = 31 * result + rollingStartIntervalNumber
            result = 31 * result + transmissionRiskLevel
            result = 31 * result + rollingPeriod
            result = 31 * result + reportType
            result = 31 * result + daysSinceOnsetOfSymptoms
            return result
        }
    }
}

fun TemporaryExposureKey.toPersistedTEK() = TEKEntryDao.PersistedTEK(
    keyData = this.keyData,
    rollingStartIntervalNumber = this.rollingStartIntervalNumber,
    transmissionRiskLevel = this.transmissionRiskLevel,
    rollingPeriod = this.rollingPeriod,
    reportType = this.reportType,
    daysSinceOnsetOfSymptoms = this.daysSinceOnsetOfSymptoms
)
