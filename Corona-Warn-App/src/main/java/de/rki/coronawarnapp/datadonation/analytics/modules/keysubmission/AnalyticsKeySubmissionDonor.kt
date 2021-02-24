package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Duration
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsKeySubmissionDonor @Inject constructor(
    val repository: AnalyticsKeySubmissionRepository,
    private val timeStamper: TimeStamper
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {

        val hours = request.currentConfig.analytics.hoursSinceTestResultToSubmitKeySubmissionMetadata
        val duration = Duration.standardHours(hours.toLong())

        return if (shouldSubmitData(duration)) {
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
            object : DonorModule.Contribution {
                override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
                    // nope
                }

                override suspend fun finishDonation(successful: Boolean) {
                    // nope
                }
            }
        }
    }

    private fun createContribution() =
        PpaData.PPAKeySubmissionMetadata.newBuilder()
            .setAdvancedConsentGiven(repository.advancedConsentGiven)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                repository.daysSinceMostRecentDateAtRiskLevelAtTestRegistration
            )
            .setHoursSinceHighRiskWarningAtTestRegistration(
                repository.hoursSinceHighRiskWarningAtTestRegistration
            )
            .setHoursSinceTestResult(repository.hoursSinceTestResult)
            .setHoursSinceTestRegistration(repository.hoursSinceTestRegistration)
            .setLastSubmissionFlowScreenValue(repository.lastSubmissionFlowScreen)
            .setSubmitted(repository.submitted)
            .setSubmittedAfterSymptomFlow(repository.submittedAfterSymptomFlow)
            .setSubmittedAfterCancel(repository.submittedAfterCancel)
            .setSubmittedInBackground(repository.submittedInBackground)
            .setSubmittedWithTeleTAN(repository.submittedWithTeleTAN)

    private fun shouldSubmitData(duration: Duration): Boolean {
        return repository.testResultReceivedAt > 0 &&
            (repository.submitted ||
                timeStamper.nowUTC.minus(duration) > Instant.ofEpochMilli(repository.testResultReceivedAt))
    }

    override suspend fun deleteData() {
        repository.reset()
    }
}
