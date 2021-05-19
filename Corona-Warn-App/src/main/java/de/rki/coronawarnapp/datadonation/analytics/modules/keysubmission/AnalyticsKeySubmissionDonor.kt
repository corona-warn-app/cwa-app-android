package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Duration
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsKeySubmissionDonor @Inject constructor(
    pcrRepository: AnalyticsPCRKeySubmissionRepository,
    raRepository: AnalyticsRAKeySubmissionRepository,
    timeStamper: TimeStamper
) : DonorModule {

    val pcrDonor = BaseAnalyticsKeySubmissionDonor(pcrRepository, timeStamper)
    val raDonor = BaseAnalyticsKeySubmissionDonor(raRepository, timeStamper)

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        return if (pcrDonor.shouldSubmitData(request))
            pcrDonor.beginDonation(request)
        else
            raDonor.beginDonation(request)
    }

    override suspend fun deleteData() {
        pcrDonor.deleteData()
        raDonor.deleteData()
    }
}

class BaseAnalyticsKeySubmissionDonor(
    private val repository: AnalyticsKeySubmissionRepository,
    private val timeStamper: TimeStamper
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {

        return if (shouldSubmitData(request)) {
            object : DonorModule.Contribution {
                override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
                    val data = createContribution()
                    protobufContainer.addKeySubmissionMetadataSet(data)
                }

                override suspend fun finishDonation(successful: Boolean) {
                    if (successful) repository.reset()
                }
            }
        } else {
            AnalyticsKeySubmissionNoContribution
        }
    }

    private fun createContribution() =
        PpaData.PPAKeySubmissionMetadata.newBuilder()
            .setAdvancedConsentGiven(repository.advancedConsentGiven)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                repository.daysSinceMostRecentDateAtEwRiskLevelAtTestRegistration
            )
            .setHoursSinceHighRiskWarningAtTestRegistration(
                repository.hoursSinceEwHighRiskWarningAtTestRegistration
            )
            .setPtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                repository.daysSinceMostRecentDateAtPtRiskLevelAtTestRegistration
            )
            .setPtHoursSinceHighRiskWarningAtTestRegistration(
                repository.hoursSincePtHighRiskWarningAtTestRegistration
            )
            .setHoursSinceTestResult(repository.hoursSinceTestResult)
            .setHoursSinceTestRegistration(repository.hoursSinceTestRegistration)
            .setLastSubmissionFlowScreenValue(repository.lastSubmissionFlowScreen)
            .setSubmitted(repository.submitted)
            .setSubmittedAfterSymptomFlow(repository.submittedAfterSymptomFlow)
            .setSubmittedAfterCancel(repository.submittedAfterCancel)
            .setSubmittedInBackground(repository.submittedInBackground)
            .setSubmittedWithTeleTAN(repository.submittedWithTeleTAN)
            .setSubmittedAfterRapidAntigenTest(repository.submittedAfterRAT)
            .setSubmittedWithCheckIns(repository.submittedWithCheckIns)

    fun shouldSubmitData(request: DonorModule.Request): Boolean {
        val hours = request.currentConfig.analytics.hoursSinceTestResultToSubmitKeySubmissionMetadata
        val timeSinceTestResultToSubmit = Duration.standardHours(hours.toLong())
        return positiveTestResultReceived && (
            keysSubmitted || enoughTimeHasPassedSinceResult(timeSinceTestResultToSubmit)
            )
    }

    private val positiveTestResultReceived: Boolean
        get() = repository.testResultReceivedAt > 0

    private val keysSubmitted: Boolean
        get() = repository.submitted

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun enoughTimeHasPassedSinceResult(timeSinceTestResultToSubmit: Duration): Boolean =
        timeStamper.nowUTC.minus(timeSinceTestResultToSubmit) > Instant.ofEpochMilli(repository.testResultReceivedAt)

    override suspend fun deleteData() {
        repository.reset()
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
object AnalyticsKeySubmissionNoContribution : DonorModule.Contribution {
    override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) = Unit
    override suspend fun finishDonation(successful: Boolean) = Unit
}
