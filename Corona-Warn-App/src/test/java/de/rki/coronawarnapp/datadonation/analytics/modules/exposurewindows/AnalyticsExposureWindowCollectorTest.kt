package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.result.RiskResult
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AnalyticsExposureWindowCollectorTest : BaseTest() {

    @MockK lateinit var analyticsExposureWindowRepository: AnalyticsExposureWindowRepository
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var exposureWindow: ExposureWindow
    @MockK lateinit var riskResult: RiskResult
    @MockK lateinit var scanInstance: ScanInstance

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { scanInstance.minAttenuationDb } returns 1
        every { scanInstance.secondsSinceLastScan } returns 1
        every { scanInstance.typicalAttenuationDb } returns 1
        every { exposureWindow.calibrationConfidence } returns 1
        every { exposureWindow.dateMillisSinceEpoch } returns 1
        every { exposureWindow.infectiousness } returns 1
        every { exposureWindow.reportType } returns 1
        every { exposureWindow.scanInstances } returns listOf(scanInstance)
        every { riskResult.normalizedTime } returns 1.0
        every { riskResult.transmissionRiskLevel } returns 1
        coEvery { analyticsExposureWindowRepository.addNew(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `data is stored when analytics enabled`() {
        every { analyticsSettings.analyticsEnabled } returns flowOf(true)
        runTest {
            newInstance().reportRiskResultsPerWindow(mapOf(exposureWindow to riskResult))
            coVerify(exactly = 1) { analyticsExposureWindowRepository.addNew(any()) }
        }
    }

    @Test
    fun `data is not stored when analytics disabled`() {
        every { analyticsSettings.analyticsEnabled } returns flowOf(false)
        runTest {
            newInstance().reportRiskResultsPerWindow(mapOf(exposureWindow to riskResult))
            coVerify(exactly = 0) { analyticsExposureWindowRepository.addNew(any()) }
        }
    }

    private fun newInstance() =
        AnalyticsExposureWindowCollector(analyticsExposureWindowRepository, analyticsSettings)
}
