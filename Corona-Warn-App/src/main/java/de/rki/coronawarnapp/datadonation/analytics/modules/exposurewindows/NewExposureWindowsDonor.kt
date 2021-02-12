package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewExposureWindowsDonor @Inject constructor(
    val repository: ExposureWindowRepository

) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {

        val data = repository.getNewContributions().map {
            val window = PpaData.PPAExposureWindow.newBuilder()
                .setDate(it.dateMillis)
                .setCalibrationConfidence(it.calibrationConfidence)
                .setInfectiousnessValue(it.infectiousness)
                .setReportTypeValue(it.reportType)

            PpaData.PPANewExposureWindow.newBuilder()
                .setExposureWindow(window)
                .setNormalizedTime(it.normalizedTime)
                .setTransmissionRiskLevel(it.transmissionRiskLevel)
                .build()
        }

        return CollectedData(
            data = data,
            onContributionFinished = { success ->
                // TODO
            }
        )
    }

    data class CollectedData(
        val data: List<PpaData.PPANewExposureWindow>,
        val onContributionFinished: suspend (Boolean) -> Unit
    ) : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
            protobufContainer.addAllNewExposureWindows(data)
        }

        override suspend fun finishDonation(successful: Boolean) {
            onContributionFinished(successful)
        }
    }
}

interface ExposureWindowRepository {
    fun getNewContributions(): List<ExposureWindowContribution>
    fun addContribution(contribution: ExposureWindowContribution)
}

data class ExposureWindowContribution constructor(
    val windowHashCode: Int,
    val calibrationConfidence: Int,
    val dateMillis: Long,
    val infectiousness: Int,
    val reportType: Int,
    val scanInstances: List<ScanInstance>,
    val normalizedTime: Double,
    val transmissionRiskLevel: Int
)

fun createExposureWindowContribution(
    window: ExposureWindow,
    result: RiskResult
): ExposureWindowContribution =
    ExposureWindowContribution(
        windowHashCode = window.hashCode(),
        calibrationConfidence = window.calibrationConfidence,
        dateMillis = window.dateMillisSinceEpoch,
        infectiousness = window.infectiousness,
        reportType = window.reportType,
        scanInstances = window.scanInstances,
        normalizedTime = result.normalizedTime,
        transmissionRiskLevel = result.transmissionRiskLevel
    )

