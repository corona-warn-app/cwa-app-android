package de.rki.coronawarnapp.datadonation.analytics

import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata.ExposureRiskMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsServer
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import de.rki.coronawarnapp.storage.LocalData
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

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)

        coEvery { appConfigProvider.getAppConfig() } returns configData
        every { configData.analytics } returns analyticsConfig
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
            settings = settings
        )
    )

    @Test
    fun `abort due to submit probability`() {
        every { analyticsConfig.probabilityToSubmit } returns 0f

        val analytics = createInstance()

        runBlockingTest2 {
            analytics.submitIfWanted()
        }

        // TODO: is there a way to make sure a spy function returned x?
        // coVerify {
        //     analytics.stopDueToProbabilityToSubmit() shouldBe true
        // }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData()
        }
    }

    @Test
    fun `abort due to last submit timestamp`() {
        every { analyticsConfig.probabilityToSubmit } returns 1f
        every { settings.lastSubmittedTimestamp } returns mockFlowPreference(Instant.now())

        val analytics = createInstance()

        runBlockingTest2 {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData()
        }
    }

    @Test
    fun `abort due to time since onboarding`() {
        every { analyticsConfig.probabilityToSubmit } returns 1f
        every { settings.lastSubmittedTimestamp } returns mockFlowPreference(
            Instant.now().minus(Days.TWO.toStandardDuration())
        )
        every { LocalData.onboardingCompletedTimestamp() } returns Instant.now().millis

        val analytics = createInstance()

        runBlockingTest2 {
            analytics.submitIfWanted()
        }

        coVerify(exactly = 0) {
            analytics.submitAnalyticsData()
        }
    }

    @Test
    fun `submit analytics data`() {
        every { analyticsConfig.probabilityToSubmit } returns 1f

        val twoDaysAgo = Instant.now().minus(Days.TWO.toStandardDuration())
        every { settings.lastSubmittedTimestamp } returns mockFlowPreference(twoDaysAgo)
        every { LocalData.onboardingCompletedTimestamp() } returns twoDaysAgo.millis

        val metadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(java.time.Instant.ofEpochSecond(101010).toEpochMilli())
            .setDateChangedComparedToPreviousSubmission(true)
            .setRiskLevelChangedComparedToPreviousSubmission(true)
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
            dataDonationAnalyticsServer.uploadAnalyticsData(any())
        }
    }
}
