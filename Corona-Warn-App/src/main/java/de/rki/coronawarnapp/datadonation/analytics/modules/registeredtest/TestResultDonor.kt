package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestResultDonor @Inject constructor(
    private val tracingStateProvider: TracingStateProvider,
    private val analyticsSettings: AnalyticsSettings
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {

        val tracingState = tracingStateProvider.state.first()

        val registrationTime = analyticsSettings.testRegistrationTime.value
        val hoursSinceTestRegistrationTime = Duration(registrationTime, Instant.now()).standardHours.toInt()

        tracingState.riskState
        val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
            .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
            // TODO verify setters below
            .setHoursSinceHighRiskWarningAtTestRegistration(0)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(0)
            .setTestResult(PpaData.PPATestResult.TEST_RESULT_POSITIVE)
            .setRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            .build()

        return TestResultMetadataContribution(testResultMetaData, ::cleanUp)
    }

    override suspend fun deleteData() = cleanUp()

    private fun cleanUp() {
        with(analyticsSettings) {
            testRegistrationTime.update { null }
            // TODO clean all saved values
        }
    }

    data class TestResultMetadataContribution(
        private val testResultMetadata: PpaData.PPATestResultMetadata,
        private val onFinishDonation: suspend () -> Unit
    ) : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
            protobufContainer.addTestResultMetadataSet(testResultMetadata)
        }

        override suspend fun finishDonation(successful: Boolean) = onFinishDonation()
    }
}
