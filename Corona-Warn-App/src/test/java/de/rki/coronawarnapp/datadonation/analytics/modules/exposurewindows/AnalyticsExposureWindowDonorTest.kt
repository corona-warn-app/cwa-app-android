package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import kotlin.random.Random

class AnalyticsExposureWindowDonorTest : BaseTest() {

    @MockK lateinit var analyticsExposureWindowRepository: AnalyticsExposureWindowRepository
    @MockK lateinit var configData: ConfigData
    private val request = object : DonorModule.Request {
        override val currentConfig: ConfigData
            get() = configData
    }
    private val testWindow = AnalyticsExposureWindowEntity(
        sha256Hash = "hash",
        calibrationConfidence = 1,
        dateMillis = 1234567890L,
        infectiousness = 2,
        reportType = 3,
        normalizedTime = 4.0,
        transmissionRiskLevel = 5
    )
    private val scanInstance = AnalyticsScanInstanceEntity(1, "hash", 1, 1, 1)
    private val wrapper = AnalyticsExposureWindowEntityWrapper(
        testWindow,
        listOf(scanInstance)
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(Random)
        coEvery { analyticsExposureWindowRepository.deleteStaleData() } just Runs
        coEvery { analyticsExposureWindowRepository.rollback(any(), any()) } just Runs
        every { Random.nextDouble() } returns .5
    }

    @Test
    fun `protobuf conversion uses correct formats`() {
        val newWindow = listOf(wrapper).asPpaData().single().apply {
            normalizedTime shouldBe 4.0
            transmissionRiskLevel shouldBe 5
        }
        newWindow.exposureWindow.apply {
            date shouldBe 1234567L
            calibrationConfidence shouldBe 1
            infectiousness shouldBe PpaData.PPAExposureWindowInfectiousness.INFECTIOUSNESS_HIGH
            reportType shouldBe PpaData.PPAExposureWindowReportType.REPORT_TYPE_SELF_REPORT
        }
    }

    @Test
    fun `skip submission when random number greater than probability`() {
        val donor = newInstance()
        runTest {
            donor.skipSubmission(.3) shouldBe true
        }
    }

    @Test
    fun `execute submission when random number less or equal than probability`() {
        val donor = newInstance()
        runTest {
            donor.skipSubmission(.5) shouldBe false
        }
    }

    @Test
    fun `skipped submission returns empty contribution`() {
        val donor = newInstance()
        coEvery { configData.analytics.probabilityToSubmitNewExposureWindows } returns .4
        runTest {
            donor.beginDonation(request) shouldBe AnalyticsExposureWindowNoContribution
        }
    }

    @Test
    fun `regular submission returns stored data`() {
        val donor = newInstance()
        val wrappers = listOf(wrapper)
        val reported = listOf(
            AnalyticsReportedExposureWindowEntity(
                "hash",
                1L
            )
        )
        coEvery { configData.analytics.probabilityToSubmitNewExposureWindows } returns .8
        coEvery { analyticsExposureWindowRepository.getAllNew() } returns wrappers
        coEvery { analyticsExposureWindowRepository.moveToReported(wrappers) } returns reported
        runTest {
            (
                donor.beginDonation(request) as AnalyticsExposureWindowDonor.Contribution
                ).data shouldBe wrappers.asPpaData()
        }
    }

    @Test
    fun `failure triggers rollback`() {
        val donor = newInstance()
        val wrappers = listOf(wrapper)
        val reported = listOf(
            AnalyticsReportedExposureWindowEntity(
                "hash",
                1L
            )
        )
        coEvery { configData.analytics.probabilityToSubmitNewExposureWindows } returns .8
        coEvery { analyticsExposureWindowRepository.getAllNew() } returns wrappers
        coEvery { analyticsExposureWindowRepository.moveToReported(wrappers) } returns reported
        runTest {
            val contribution = donor.beginDonation(request)
            contribution.finishDonation(false)
            coVerify { analyticsExposureWindowRepository.rollback(wrappers, reported) }
        }
    }

    @Test
    fun `stale data clean up`() {
        val donor = newInstance()
        coEvery { configData.analytics.probabilityToSubmitNewExposureWindows } returns .4
        runTest {
            donor.beginDonation(request)
            coVerify { analyticsExposureWindowRepository.deleteStaleData() }
        }
    }

    private fun newInstance() =
        AnalyticsExposureWindowDonor(
            analyticsExposureWindowRepository
        )
}
