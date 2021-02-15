package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewExposureWindowsDonor @Inject constructor(
    val repository: ExposureWindowRepository
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val newContributions = repository.getNewContributions()
        return Contribution(
            data = newContributions.asPpaData(),
            onContributionFinished = { success ->
                if (success) repository.moveToReported(newContributions)
            }
        )
    }

    override suspend fun deleteData() {
        // TODO
    }

    data class Contribution(
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

private fun List<ExposureWindowContribution>.asPpaData() = map {
    val scanInstances = it.scanInstances.map { scanInstance ->
        PpaData.PPAExposureWindowScanInstance.newBuilder()
            .setMinAttenuation(scanInstance.minAttenuation)
            .setTypicalAttenuation(scanInstance.typicalAttenuation)
            .setSecondsSinceLastScan(scanInstance.secondsSinceLastScan)
            .build()
    }

    val exposureWindow = PpaData.PPAExposureWindow.newBuilder()
        .setDate(it.dateMillis)
        .setCalibrationConfidence(it.calibrationConfidence)
        .setInfectiousnessValue(it.infectiousness)
        .setReportTypeValue(it.reportType)
        .addAllScanInstances(scanInstances)
        .build()

    PpaData.PPANewExposureWindow.newBuilder()
        .setExposureWindow(exposureWindow)
        .setNormalizedTime(it.normalizedTime)
        .setTransmissionRiskLevel(it.transmissionRiskLevel)
        .build()
}
