package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AnalyticsExposureWindowDonor @Inject constructor(
    private val analyticsExposureWindowRepository: AnalyticsExposureWindowRepository
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        // clean up
        analyticsExposureWindowRepository.deleteStaleData()

        val probability = request.currentConfig.analytics.probabilityToSubmitNewExposureWindows
        if (skipSubmission(probability)) {
            Timber.w("Submission skipped.")
            return AnalyticsExposureWindowNoContribution
        }

        val newWrappers = analyticsExposureWindowRepository.getAllNew()
        val reported = analyticsExposureWindowRepository.moveToReported(newWrappers)
        return Contribution(
            data = newWrappers.asPpaData(),
            onDonationFailed = { onDonationFailed(newWrappers, reported) }
        )
    }

    override suspend fun deleteData() {
        analyticsExposureWindowRepository.deleteAllData()
    }

    @VisibleForTesting
    internal fun skipSubmission(probability: Double): Boolean {
        // load balancing
        val random = Random.nextDouble()
        Timber.w("Random number is $random. probabilityToSubmitNewExposureWindows is $probability.")
        return random > probability
    }

    @VisibleForTesting
    internal suspend fun onDonationFailed(
        newWrappers: List<AnalyticsExposureWindowEntityWrapper>,
        reported: List<AnalyticsReportedExposureWindowEntity>
    ) {
        analyticsExposureWindowRepository.rollback(newWrappers, reported)
    }

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

@VisibleForTesting
internal fun List<AnalyticsExposureWindowEntityWrapper>.asPpaData() = map {
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

@VisibleForTesting
object AnalyticsExposureWindowNoContribution : DonorModule.Contribution {
    override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) = Unit
    override suspend fun finishDonation(successful: Boolean) = Unit
}
