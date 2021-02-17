package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class NewExposureWindowsDonor @Inject constructor(
    private val repository: AnalyticsExposureWindowRepository,
    private val appConfigProvider: AppConfigProvider
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        if (skipSubmission()) {
            Timber.w("Submission skipped.")
            return emptyContribution()
        }

        val newWrappers = repository.getAllNew()
        val reported = repository.moveToReported(newWrappers)
        return Contribution(
            data = newWrappers.asPpaData(),
            onDonationFailed = {
                repository.rollback(newWrappers, reported)
            }
        )
    }

    override suspend fun deleteData() {
        repository.deleteAllData()
    }

    private suspend fun skipSubmission(): Boolean {
        // Skip if generated random number between 0 and 1 is greater than
        // the value of Configuration Parameter probabilityToSubmitExposureWindows.
        val random = Random.nextDouble()
        val configData: ConfigData = appConfigProvider.getAppConfig()
        val probability = configData.analytics.probabilityToSubmitNewExposureWindows
        Timber.w("Random number is $random. probabilityToSubmitNewExposureWindows is $probability")
        return random > probability
    }

    private fun emptyContribution() = Contribution(
        data = emptyList(),
        onDonationFailed = {}
    )

    data class Contribution(
        val data: List<PpaData.PPANewExposureWindow>,
        val onDonationFailed: suspend () -> Unit
    ) : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
            protobufContainer.addAllNewExposureWindows(data)
        }

        override suspend fun finishDonation(successful: Boolean) {
            if (!successful) onDonationFailed()
        }
    }
}

private fun List<AnalyticsExposureWindowEntityWrapper>.asPpaData() = map {
    val scanInstances = it.scanInstanceEntities.map { scanInstance ->
        PpaData.PPAExposureWindowScanInstance.newBuilder()
            .setMinAttenuation(scanInstance.minAttenuation)
            .setTypicalAttenuation(scanInstance.typicalAttenuation)
            .setSecondsSinceLastScan(scanInstance.secondsSinceLastScan)
            .build()
    }

    val exposureWindow = PpaData.PPAExposureWindow.newBuilder()
        .setDate(it.exposureWindowEntity.dateMillis)
        .setCalibrationConfidence(it.exposureWindowEntity.calibrationConfidence)
        .setInfectiousnessValue(it.exposureWindowEntity.infectiousness)
        .setReportTypeValue(it.exposureWindowEntity.reportType)
        .addAllScanInstances(scanInstances)
        .build()

    PpaData.PPANewExposureWindow.newBuilder()
        .setExposureWindow(exposureWindow)
        .setNormalizedTime(it.exposureWindowEntity.normalizedTime)
        .setTransmissionRiskLevel(it.exposureWindowEntity.transmissionRiskLevel)
        .build()
}
