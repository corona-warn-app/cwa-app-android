package de.rki.coronawarnapp.datadonation.analytics

import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata.ExposureRiskMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsServer
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.spyk
import org.joda.time.Days
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.preferences.mockFlowPreference

class AnalyticsTest : BaseTest() {
    @MockK lateinit var dataDonationAnalyticsServer: DataDonationAnalyticsServer
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var deviceAttestation: DeviceAttestation
    @MockK lateinit var settings: AnalyticsSettings
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var analyticsConfig: AnalyticsConfig
    @MockK lateinit var exposureRiskMetadataDonor: ExposureRiskMetadataDonor
    @MockK lateinit var lastAnalyticsSubmissionLogger: LastAnalyticsSubmissionLogger
    @MockK lateinit var timeStamper: TimeStamper

    private val baseTime: Instant = Instant.ofEpochMilli(0)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)

        coEvery { appConfigProvider.getAppConfig() } returns configData
        every { configData.analytics } returns analyticsConfig

        coEvery { lastAnalyticsSubmissionLogger.storeAnalyticsData(any()) } just Runs

        every { timeStamper.nowUTC } returns baseTime

        every { analyticsConfig.analyticsEnabled } returns true

        every { settings.analyticsEnabled } returns mockFlowPreference(true)
        every { analyticsConfig.probabilityToSubmit } returns 1.0

        val twoDaysAgo = baseTime.minus(Days.TWO.toStandardDuration())
        every { settings.lastSubmittedTimestamp } returns mockFlowPreference(twoDaysAgo)
        every { LocalData.onboardingCompletedTimestamp() } returns twoDaysAgo.millis

        every { analyticsConfig.safetyNetRequirements } returns SafetyNetRequirementsContainer()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createDonorModules(): Set<DonorModule> = setOf(exposureRiskMetadataDonor)

    private fun createInstance() = spyk(
        Analytics(
            dataDonationAnalyticsServer = dataDonationAnalyticsServer,
            appConfigProvider = appConfigProvider,
            deviceAttestation = deviceAttestation,
            donorModules = createDonorModules(),
            settings = settings,
            logger = lastAnalyticsSubmissionLogger,
            timeStamper = timeStamper
        )
    )

    @Test
    fun `abort due to no app config`() {
        every { analyticsConfig.analyticsEnabled } returns false

        val analytics = createInstance()

        runBlockingTest2 {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData()
            analytics.stopDueToNoUserConsent()
        }
    }

    @Test
    fun `abort due to no user consent`() {
        every { settings.analyticsEnabled } returns mockFlowPreference(false)

        val analytics = createInstance()

        runBlockingTest2 {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig()
            analytics.stopDueToNoUserConsent()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData()
            analytics.stopDueToProbabilityToSubmit()
        }
    }

    @Test
    fun `abort due to submit probability`() {
        every { analyticsConfig.probabilityToSubmit } returns 0.0

        val analytics = createInstance()

        runBlockingTest2 {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig()
            analytics.stopDueToNoUserConsent()
            analytics.stopDueToProbabilityToSubmit()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData()
            analytics.stopDueToLastSubmittedTimestamp()
        }
    }

    @Test
    fun `abort due to last submit timestamp`() {
        every { settings.lastSubmittedTimestamp } returns mockFlowPreference(Instant.now())

        val analytics = createInstance()

        runBlockingTest2 {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig()
            analytics.stopDueToNoUserConsent()
            analytics.stopDueToProbabilityToSubmit()
            analytics.stopDueToLastSubmittedTimestamp()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData()
            analytics.stopDueToTimeSinceOnboarding()
        }
    }

    @Test
    fun `abort due to time since onboarding`() {
        every { LocalData.onboardingCompletedTimestamp() } returns baseTime.millis

        val analytics = createInstance()

        runBlockingTest2 {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 1) {
            analytics.stopDueToNoAnalyticsConfig()
            analytics.stopDueToNoUserConsent()
            analytics.stopDueToProbabilityToSubmit()
            analytics.stopDueToLastSubmittedTimestamp()
            analytics.stopDueToTimeSinceOnboarding()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData()
        }
    }

    @Test
    fun `submit analytics data`() {
        val metadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseTime.millis)
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


        coEvery { exposureRiskMetadataDonor.beginDonation(any()) } returns
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

        coEvery { dataDonationAnalyticsServer.uploadAnalyticsData(any()) } just Runs

        val analytics = createInstance()

        runBlockingTest2 {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 1) {
            analytics.submitAnalyticsData()
            dataDonationAnalyticsServer.uploadAnalyticsData(analyticsRequest)
        }
    }
}
