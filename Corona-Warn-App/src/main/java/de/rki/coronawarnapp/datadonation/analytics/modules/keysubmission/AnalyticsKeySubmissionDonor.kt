package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.TriStateBooleanOuterClass
import de.rki.coronawarnapp.util.TimeStamper
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsPCRKeySubmissionDonor @Inject constructor(
    pcrRepository: AnalyticsPCRKeySubmissionRepository,
    timeStamper: TimeStamper
) : AnalyticsKeySubmissionDonor(pcrRepository, timeStamper)

@Singleton
class AnalyticsRAKeySubmissionDonor @Inject constructor(
    raRepository: AnalyticsRAKeySubmissionRepository,
    timeStamper: TimeStamper
) : AnalyticsKeySubmissionDonor(raRepository, timeStamper)

abstract class AnalyticsKeySubmissionDonor(
    private val repository: AnalyticsKeySubmissionRepository,
    private val timeStamper: TimeStamper
) : DonorModule {
    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val hours = request.currentConfig.analytics.hoursSinceTestResultToSubmitKeySubmissionMetadata
        val timeSinceTestResultToSubmit = Duration.ofHours(hours.toLong())
        return if (shouldSubmitData(timeSinceTestResultToSubmit)) {
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
                repository.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
            )
            .setHoursSinceHighRiskWarningAtTestRegistration(
                repository.ewHoursSinceHighRiskWarningAtTestRegistration
            )
            .setPtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                repository.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
            )
            .setPtHoursSinceHighRiskWarningAtTestRegistration(
                repository.ptHoursSinceHighRiskWarningAtTestRegistration
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
            .setSubmittedWithCheckIns(repository.submittedWithCheckIns.toTriStateBoolean())

    fun shouldSubmitData(timeSinceTestResultToSubmit: Duration): Boolean {
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
        timeStamper
            .nowUTC
            .minus(timeSinceTestResultToSubmit) > Instant.ofEpochMilli(repository.testResultReceivedAt)

    override suspend fun deleteData() {
        repository.reset()
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
object AnalyticsKeySubmissionNoContribution : DonorModule.Contribution {
    override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) = Unit
    override suspend fun finishDonation(successful: Boolean) = Unit
}

private fun Boolean?.toTriStateBoolean() =
    when (this) {
        true -> TriStateBooleanOuterClass.TriStateBoolean.TSB_TRUE
        false -> TriStateBooleanOuterClass.TriStateBoolean.TSB_FALSE
        null -> TriStateBooleanOuterClass.TriStateBoolean.TSB_UNSPECIFIED
    }
