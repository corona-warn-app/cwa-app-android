package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import kotlin.random.Random

class AnalyticsExposureWindowDonorTest : BaseTest() {

    @MockK lateinit var analyticsExposureWindowRepository: AnalyticsExposureWindowRepository
    @MockK lateinit var appConfigProvider: AppConfigProvider
    private val request = object : DonorModule.Request {}
    private val window = AnalyticsExposureWindowEntity(
        "hash",
        1,
        1L,
        1,
        1,
        1.0,
        1
    )
    private val scanInstance = AnalyticsScanInstanceEntity(1, "hash", 1, 1, 1)
    private val wrapper = AnalyticsExposureWindowEntityWrapper(
        window,
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

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `skip submission when random number greater than probability`() {
        val donor = newInstance()
        coEvery { appConfigProvider.getAppConfig().analytics.probabilityToSubmitNewExposureWindows } returns .3
        runBlockingTest {
            donor.skipSubmission() shouldBe true
        }
    }

    @Test
    fun `execute submission when random number less or equal than probability`() {
        val donor = newInstance()
        coEvery { appConfigProvider.getAppConfig().analytics.probabilityToSubmitNewExposureWindows } returns .5
        runBlockingTest {
            donor.skipSubmission() shouldBe false
        }
    }

    @Test
    fun `skipped submission returns empty contribution`() {
        val donor = newInstance()
        coEvery { appConfigProvider.getAppConfig().analytics.probabilityToSubmitNewExposureWindows } returns .4
        runBlockingTest {
            donor.beginDonation(request) shouldBe donor.emptyContribution
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
        coEvery { appConfigProvider.getAppConfig().analytics.probabilityToSubmitNewExposureWindows } returns .8
        coEvery { analyticsExposureWindowRepository.getAllNew() } returns wrappers
        coEvery { analyticsExposureWindowRepository.moveToReported(wrappers) } returns reported
        runBlockingTest {
            (donor.beginDonation(request) as AnalyticsExposureWindowDonor.Contribution).data shouldBe wrappers.asPpaData()
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
        coEvery { appConfigProvider.getAppConfig().analytics.probabilityToSubmitNewExposureWindows } returns .8
        coEvery { analyticsExposureWindowRepository.getAllNew() } returns wrappers
        coEvery { analyticsExposureWindowRepository.moveToReported(wrappers) } returns reported
        runBlockingTest {
            val contribution = donor.beginDonation(request)
            contribution.finishDonation(false)
            coVerify { analyticsExposureWindowRepository.rollback(wrappers, reported) }
        }
    }

    @Test
    fun `stale data clean up`() {
        val donor = newInstance()
        coEvery { appConfigProvider.getAppConfig().analytics.probabilityToSubmitNewExposureWindows } returns .4
        runBlockingTest {
            donor.beginDonation(request)
            coVerify { analyticsExposureWindowRepository.deleteStaleData() }
        }
    }

    private fun newInstance() =
        AnalyticsExposureWindowDonor(
            analyticsExposureWindowRepository,
            appConfigProvider
        )

    companion object {
        @AfterAll
        fun cleanup() {
            unmockkAll()
        }
    }
}
