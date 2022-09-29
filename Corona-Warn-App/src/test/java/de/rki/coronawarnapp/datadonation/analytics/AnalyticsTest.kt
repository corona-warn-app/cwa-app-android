package de.rki.coronawarnapp.datadonation.analytics

import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata.ExposureRiskMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.usermetadata.UserMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsServer
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference
import java.time.Duration

class AnalyticsTest : BaseTest() {
    @MockK lateinit var dataDonationAnalyticsServer: DataDonationAnalyticsServer
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var deviceAttestation: DeviceAttestation
    @MockK lateinit var settings: AnalyticsSettings
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var analyticsConfig: AnalyticsConfig
    @RelaxedMockK lateinit var exposureRiskMetadataDonor: ExposureRiskMetadataDonor
    @MockK lateinit var lastAnalyticsSubmissionLogger: LastAnalyticsSubmissionLogger
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var onboardingSettings: OnboardingSettings

    private val baseTime: Instant = Instant.ofEpochMilli(0)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { appConfigProvider.getAppConfig() } returns configData
        every { configData.analytics } returns analyticsConfig

        coEvery { lastAnalyticsSubmissionLogger.storeAnalyticsData(any()) } just Runs

        every { timeStamper.nowUTC } returns baseTime

        every { analyticsConfig.analyticsEnabled } returns true

        every { settings.analyticsEnabled } returns mockFlowPreference(true)
        every { analyticsConfig.probabilityToSubmit } returns 1.0

        val twoDaysAgo = baseTime.minus(Duration.ofDays(2))
        every { settings.lastSubmittedTimestamp } returns mockFlowPreference(twoDaysAgo)
        every { onboardingSettings.onboardingCompletedTimestamp } returns flowOf(twoDaysAgo)

        every { analyticsConfig.safetyNetRequirements } returns SafetyNetRequirementsContainer()

        coEvery { dataDonationAnalyticsServer.uploadAnalyticsData(any()) } just Runs
    }

    private fun createInstance(modules: Set<DonorModule> = setOf(exposureRiskMetadataDonor)) = spyk(
        Analytics(
            dataDonationAnalyticsServer = dataDonationAnalyticsServer,
            appConfigProvider = appConfigProvider,
            deviceAttestation = deviceAttestation,
            donorModules = modules,
            settings = settings,
            logger = lastAnalyticsSubmissionLogger,
            timeStamper = timeStamper,
            onboardingSettings = onboardingSettings
        )
    )

    @Test
    fun `abort due to no app config`() {
        every { analyticsConfig.analyticsEnabled } returns false

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.apply {
                successful shouldBe false
                shouldRetry shouldBe false
            }
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig(analyticsConfig)
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData(configData)
            analytics.stopDueToNoUserConsent()
        }
    }

    @Test
    fun `abort due to no user consent`() {
        every { settings.analyticsEnabled } returns mockFlowPreference(false)

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.apply {
                successful shouldBe false
                shouldRetry shouldBe false
            }
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig(analyticsConfig)
            analytics.stopDueToNoUserConsent()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData(configData)
            analytics.stopDueToProbabilityToSubmit(analyticsConfig)
        }
    }

    @Test
    fun `abort due to submit probability`() {
        every { analyticsConfig.probabilityToSubmit } returns 0.0

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.apply {
                successful shouldBe false
                shouldRetry shouldBe false
            }
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig(analyticsConfig)
            analytics.stopDueToNoUserConsent()
            analytics.stopDueToProbabilityToSubmit(analyticsConfig)
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData(configData)
            analytics.stopDueToLastSubmittedTimestamp()
        }
    }

    @Test
    fun `abort due to last submit timestamp`() {
        every { settings.lastSubmittedTimestamp } returns mockFlowPreference(Instant.now())

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.apply {
                successful shouldBe false
                shouldRetry shouldBe false
            }
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig(analyticsConfig)
            analytics.stopDueToNoUserConsent()
            analytics.stopDueToProbabilityToSubmit(analyticsConfig)
            analytics.stopDueToLastSubmittedTimestamp()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData(configData)
            analytics.stopDueToTimeSinceOnboarding()
        }
    }

    @Test
    fun `abort due to time since onboarding`() {
        every { onboardingSettings.onboardingCompletedTimestamp } returns flowOf(baseTime)

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.apply {
                successful shouldBe false
                shouldRetry shouldBe false
            }
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig(analyticsConfig)
            analytics.stopDueToNoUserConsent()
            analytics.stopDueToProbabilityToSubmit(analyticsConfig)
            analytics.stopDueToLastSubmittedTimestamp()
            analytics.stopDueToTimeSinceOnboarding()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData(configData)
        }
    }

    @Test
    fun `submit analytics data`() {
        val metadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseTime.toEpochMilli())
            .setDateChangedComparedToPreviousSubmission(true)
            .setRiskLevelChangedComparedToPreviousSubmission(true)
            .build()

        val payload = PpaData.PPADataAndroid.newBuilder()
            .addExposureRiskMetadataSet(metadata)
            .build()

        val analyticsRequest = PpaDataRequestAndroid.PPADataRequestAndroid.newBuilder()
            .setPayload(payload)
            .setAuthentication(PpacAndroid.PPACAndroid.getDefaultInstance())
            .build()

        val donationRequestSlot = slot<DonorModule.Request>()
        coEvery { exposureRiskMetadataDonor.beginDonation(capture(donationRequestSlot)) } returns
            ExposureRiskMetadataDonor.ExposureRiskMetadataContribution(
                contributionProto = metadata,
                onContributionFinished = {}
            )

        coEvery { deviceAttestation.attest(any()) } returns
            object : DeviceAttestation.Result {
                override val accessControlProtoBuf: PpacAndroid.PPACAndroid
                    get() = PpacAndroid.PPACAndroid.getDefaultInstance()

                override fun requirePass(requirements: SafetyNetRequirements) {}
            }

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.apply {
                successful shouldBe true
                shouldRetry shouldBe false
            }
        }

        donationRequestSlot.captured.currentConfig shouldBe configData

        coVerify(exactly = 1) {
            analytics.submitAnalyticsData(configData)
            dataDonationAnalyticsServer.uploadAnalyticsData(analyticsRequest)
        }
    }

    @Test
    fun `despite error on beginDonation modules can still cleanup`() {
        val userMetadataDonor = mockk<UserMetadataDonor>().apply {
            coEvery { beginDonation(any()) } throws Exception("KABOOM!")
        }
        val modules = setOf(exposureRiskMetadataDonor, userMetadataDonor)

        val mockExposureRisk = mockk<DonorModule.Contribution>().apply {
            coEvery { injectData(any()) } just Runs
            coEvery { finishDonation(any()) } just Runs
        }
        coEvery { exposureRiskMetadataDonor.beginDonation(any()) } returns mockExposureRisk

        coEvery { deviceAttestation.attest(any()) } returns object : DeviceAttestation.Result {
            override val accessControlProtoBuf: PpacAndroid.PPACAndroid
                get() = PpacAndroid.PPACAndroid.getDefaultInstance()

            override fun requirePass(requirements: SafetyNetRequirements) {}
        }

        val analytics = createInstance(modules = modules)

        runTest {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 1) {
            userMetadataDonor.beginDonation(any())

            exposureRiskMetadataDonor.beginDonation(any())
            mockExposureRisk.injectData(any())
            mockExposureRisk.finishDonation(true)

            analytics.submitAnalyticsData(any())
        }
    }

    @Test
    fun `despite errors during donation modules can still cleanup`() {
        val userMetaDataDonation = mockk<DonorModule.Contribution>().apply {
            coEvery { injectData(any()) } throws Exception("KAPOW!")
            coEvery { finishDonation(any()) } throws Exception("CRUNCH!")
        }
        val userMetadataDonor = mockk<UserMetadataDonor>().apply {
            coEvery { beginDonation(any()) } returns userMetaDataDonation
        }
        val modules = setOf(exposureRiskMetadataDonor, userMetadataDonor)

        val exposureRiskDonation = mockk<ExposureRiskMetadataDonor.ExposureRiskMetadataContribution>().apply {
            coEvery { injectData(any()) } just Runs
            coEvery { finishDonation(any()) } just Runs
        }
        coEvery { exposureRiskMetadataDonor.beginDonation(any()) } returns exposureRiskDonation

        coEvery { deviceAttestation.attest(any()) } returns object : DeviceAttestation.Result {
            override val accessControlProtoBuf: PpacAndroid.PPACAndroid
                get() = PpacAndroid.PPACAndroid.getDefaultInstance()

            override fun requirePass(requirements: SafetyNetRequirements) {}
        }

        val analytics = createInstance(modules = modules)

        runTest {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 1) {
            exposureRiskMetadataDonor.beginDonation(any())
            exposureRiskDonation.injectData(any())
            exposureRiskDonation.finishDonation(true)

            userMetadataDonor.beginDonation(any())
            userMetaDataDonation.injectData(any())
            userMetaDataDonation.finishDonation(true)

            analytics.submitAnalyticsData(any())
        }
    }

    @Test
    fun `we catch safetynet timeout and enable retry`() {
        val exposureRiskDonation = mockk<ExposureRiskMetadataDonor.ExposureRiskMetadataContribution>().apply {
            coEvery { injectData(any()) } just Runs
            coEvery { finishDonation(any()) } just Runs
        }
        coEvery { exposureRiskMetadataDonor.beginDonation(any()) } returns exposureRiskDonation

        coEvery { deviceAttestation.attest(any()) } throws SafetyNetException(
            type = SafetyNetException.Type.ATTESTATION_REQUEST_FAILED,
            "Timeout???",
            cause = Exception()
        )

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.successful shouldBe false
            result.shouldRetry shouldBe true
        }

        coVerify(exactly = 1) {
            exposureRiskMetadataDonor.beginDonation(any())
            exposureRiskDonation.injectData(any())
            exposureRiskDonation.finishDonation(false)
        }

        coVerify(exactly = 0) { dataDonationAnalyticsServer.uploadAnalyticsData(any()) }
    }

    @Test
    fun `overall submission can timeout on safetynet and still allow modules to cleanup`() {
        val exposureRiskDonation = mockk<ExposureRiskMetadataDonor.ExposureRiskMetadataContribution>().apply {
            coEvery { injectData(any()) } just Runs
            coEvery { finishDonation(any()) } just Runs
        }
        coEvery { exposureRiskMetadataDonor.beginDonation(any()) } returns exposureRiskDonation

        coEvery { deviceAttestation.attest(any()) } coAnswers {
            // Timeout should be 360s
            delay(370_000)
            mockk()
        }

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.successful shouldBe false
            result.shouldRetry shouldBe true
        }

        coVerify(exactly = 1) {
            exposureRiskMetadataDonor.beginDonation(any())
            exposureRiskDonation.injectData(any())
            exposureRiskDonation.finishDonation(false)
        }

        coVerify(exactly = 0) {
            dataDonationAnalyticsServer.uploadAnalyticsData(any())
        }
    }

    @Test
    fun `overall submission can timeout on upload and still allow modules to cleanup`() {
        val exposureRiskDonation = mockk<ExposureRiskMetadataDonor.ExposureRiskMetadataContribution>().apply {
            coEvery { injectData(any()) } just Runs
            coEvery { finishDonation(any()) } just Runs
        }
        coEvery { exposureRiskMetadataDonor.beginDonation(any()) } returns exposureRiskDonation

        coEvery { deviceAttestation.attest(any()) } returns object : DeviceAttestation.Result {
            override val accessControlProtoBuf: PpacAndroid.PPACAndroid
                get() = PpacAndroid.PPACAndroid.getDefaultInstance()

            override fun requirePass(requirements: SafetyNetRequirements) {}
        }

        coEvery { dataDonationAnalyticsServer.uploadAnalyticsData(any()) } coAnswers {
            // Timeout should be 360s
            delay(370_000)
            mockk()
        }

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.successful shouldBe false
            result.shouldRetry shouldBe true
        }

        coVerify(exactly = 1) {
            exposureRiskMetadataDonor.beginDonation(any())
            exposureRiskDonation.injectData(any())
            exposureRiskDonation.finishDonation(false)
            dataDonationAnalyticsServer.uploadAnalyticsData(any())
        }
    }

    @Test
    fun `we catch safetynet internal error and enable retry`() {
        val exposureRiskDonation = mockk<ExposureRiskMetadataDonor.ExposureRiskMetadataContribution>().apply {
            coEvery { injectData(any()) } just Runs
            coEvery { finishDonation(any()) } just Runs
        }
        coEvery { exposureRiskMetadataDonor.beginDonation(any()) } returns exposureRiskDonation

        coEvery { deviceAttestation.attest(any()) } throws SafetyNetException(
            type = SafetyNetException.Type.INTERNAL_ERROR,
            "Timeout???",
            cause = Exception()
        )

        val analytics = createInstance()

        runTest {
            val result = analytics.submitIfWanted()
            result.successful shouldBe false
            result.shouldRetry shouldBe true
        }

        coVerify(exactly = 1) {
            exposureRiskMetadataDonor.beginDonation(any())
            exposureRiskDonation.injectData(any())
            exposureRiskDonation.finishDonation(false)
        }

        coVerify(exactly = 0) { dataDonationAnalyticsServer.uploadAnalyticsData(any()) }
    }

    @Test
    fun `reset leads to deletion of all data for each module`() = runTest {
        val userMetadataDonor = mockk<UserMetadataDonor>(relaxed = true)
        val modules = setOf(exposureRiskMetadataDonor, userMetadataDonor)

        createInstance(modules).reset()

        coVerify { modules.forEach { it.deleteData() } }
    }
}
