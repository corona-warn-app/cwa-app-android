package de.rki.coronawarnapp.nearby.windows

import com.google.android.gms.common.internal.safeparcel.SafeParcelable
import com.google.android.gms.internal.nearby.zzmy
import com.google.android.gms.nearby.exposurenotification.CalibrationConfidence
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.Infectiousness
import com.google.android.gms.nearby.exposurenotification.ReportType
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import com.google.gson.Gson
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.nearby.windows.entities.ExposureWindowsJsonInput
import de.rki.coronawarnapp.nearby.windows.entities.cases.JsonWindow
import de.rki.coronawarnapp.nearby.windows.entities.cases.TestCase
import de.rki.coronawarnapp.risk.DefaultRiskLevels
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import org.joda.time.DateTimeConstants
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.io.FileReader
import java.nio.file.Paths

class ExposureWindowsCalculationTest: BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    private lateinit var riskLevels: DefaultRiskLevels

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `init`() {
        val jsonFile = Paths.get("src", "test", "resources", "exposure-windows-risk-calculation.json").toFile()
        jsonFile shouldNotBe null

        val jsonString =  FileReader(jsonFile).readText()
        jsonString.length shouldBeGreaterThan 0

        val json = Gson().fromJson<ExposureWindowsJsonInput>(jsonString, ExposureWindowsJsonInput::class.java)
        json shouldNotBe null

        // Check configuration
        json.defaultRiskCalculationConfiguration shouldNotBe null
        json.defaultRiskCalculationConfiguration.minutesAtAttenuationFilters.size shouldBeGreaterThan 0
        json.defaultRiskCalculationConfiguration.minutesAtAttenuationWeights.size shouldBeGreaterThan 0
        json.defaultRiskCalculationConfiguration.normalizedTimePerDayToRiskLevelMappingList.size shouldBeGreaterThan 0
        json.defaultRiskCalculationConfiguration.normalizedTimePerExposureWindowToRiskLevelMapping.size shouldBeGreaterThan 0
        json.defaultRiskCalculationConfiguration.transmissionRiskLevelMultiplier shouldNotBe null
        json.defaultRiskCalculationConfiguration.transmissionRiskLevelEncoding shouldNotBe null
        json.defaultRiskCalculationConfiguration.transmissionRiskLevelFilters.size shouldBeGreaterThan 0

        Timber.v("Configuration: checked")

        json.testCases.map { case -> checkTestCase(case) }

        Timber.v("Test cases checked. Total count: ${json.testCases.size}")

        coEvery { appConfigProvider.getAppConfig() } returns json.defaultRiskCalculationConfiguration

        every {appConfigProvider.currentConfig} returns flow {configData}

        riskLevels = DefaultRiskLevels(appConfigProvider)

        val allExposureWindows = mutableListOf<ExposureWindow>()
        for (case: TestCase in json.testCases) {
            val exposureWindows: List<ExposureWindow> = case.exposureWindows.map { window -> jsonToExposureWindow(window) }
            allExposureWindows.addAll(exposureWindows)
        }

        // Check that we actually have different exposure windows
        for (exposureWindow:ExposureWindow in allExposureWindows) {
            Timber.v(
                "Final Exposure window: #%s\n-Calibration Confidence: %s\n-Date Millis Since Epoch: %s\n-Infectiousness: %s\n-Report type: %s",
                exposureWindow.hashCode(),
                exposureWindow.calibrationConfidence,
                exposureWindow.dateMillisSinceEpoch,
                exposureWindow.infectiousness,
                exposureWindow.reportType
            )
        }
        Timber.v("Duplicates: ${allExposureWindows.groupingBy { it }.eachCount().filter { it.value > 1 }}")
    }

    private fun checkTestCase(case: TestCase) {
        Timber.v("Checking ${case.description}")

        case.expTotalRiskLevel shouldNotBe null
        case.expTotalMinimumDistinctEncountersWithLowRisk shouldNotBe null
        case.expTotalMinimumDistinctEncountersWithHighRisk shouldNotBe null

        case.exposureWindows.map { exposureWindow -> checkExposureWindow(exposureWindow) }
    }

    private fun checkExposureWindow(jsonWindow: JsonWindow) {
        jsonWindow.ageInDays shouldNotBe null
        jsonWindow.reportType shouldNotBe null
        jsonWindow.infectiousness shouldNotBe null
        jsonWindow.calibrationConfidence shouldNotBe null
    }

    private fun jsonToExposureWindow(json: JsonWindow) : ExposureWindow {
        val exposureWindow: ExposureWindow = mockk()

        every { exposureWindow.calibrationConfidence } returns json.calibrationConfidence
        every { exposureWindow.dateMillisSinceEpoch } returns (DateTimeConstants.MILLIS_PER_DAY * json.ageInDays).toLong()
        every { exposureWindow.infectiousness } returns json.infectiousness
        every { exposureWindow.reportType } returns json.reportType

        Timber.v(
            "Mocking Exposure window: #%s\n-Calibration Confidence: %s\n-Date Millis Since Epoch: %s\n-Infectiousness: %s\n-Report type: %s",
            exposureWindow.hashCode(),
            exposureWindow.calibrationConfidence,
            exposureWindow.dateMillisSinceEpoch,
            exposureWindow.infectiousness,
            exposureWindow.reportType
        )

        return exposureWindow
    }

}
