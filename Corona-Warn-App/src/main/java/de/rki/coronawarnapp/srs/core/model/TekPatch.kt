package de.rki.coronawarnapp.srs.core.model

import android.os.Parcelable
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TekPatch(
    val parcelableTeks: List<ParcelableTek>
) : Parcelable {

    fun osKeys(): List<TemporaryExposureKey> = parcelableTeks.map { pKey -> pKey.toTemporaryExposureKey() }

    @Parcelize
    data class ParcelableTek(
        val keyData: ByteArray,
        val rollingStartIntervalNumber: Int,
        val transmissionRiskLevel: Int,
        val rollingPeriod: Int,
        val reportType: Int,
        val daysSinceOnsetOfSymptoms: Int
    ) : Parcelable {

        fun toTemporaryExposureKey(): TemporaryExposureKey = TemporaryExposureKey.TemporaryExposureKeyBuilder()
            .apply {
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

            other as ParcelableTek

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

    companion object {
        fun patchFrom(osKeys: List<TemporaryExposureKey>) = TekPatch(
            parcelableTeks = osKeys.map { oskey -> oskey.toParcelableTek() }
        )
    }
}

fun TemporaryExposureKey.toParcelableTek() = TekPatch.ParcelableTek(
    keyData = this.keyData,
    rollingStartIntervalNumber = this.rollingStartIntervalNumber,
    transmissionRiskLevel = this.transmissionRiskLevel,
    rollingPeriod = this.rollingPeriod,
    reportType = this.reportType,
    daysSinceOnsetOfSymptoms = this.daysSinceOnsetOfSymptoms
)
