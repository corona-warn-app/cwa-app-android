package de.rki.coronawarnapp.nearby.windows

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import com.google.gson.Gson
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.nearby.windows.entities.ExposureWindowsJsonInput
import de.rki.coronawarnapp.nearby.windows.entities.cases.JsonScanInstance
import de.rki.coronawarnapp.nearby.windows.entities.cases.JsonWindow
import de.rki.coronawarnapp.nearby.windows.entities.cases.TestCase
import de.rki.coronawarnapp.risk.DefaultRiskLevels
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.RiskResult
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
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

    // Json file (located in /test/resources/exposure-windows-risk-calculation.json)
    private val fileName = "exposure-windows-risk-calculation.json"

    // Debug logs
    private enum class LogLevel(val value: Int) {
        NONE(0),
        ONLY_COMPARISON(1),
        ALL(2)
    }
    private val logLevel = LogLevel.ONLY_COMPARISON

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun debugLog(s: String, toShow: LogLevel) {
        if (logLevel != toShow)
            return
        Timber.v(s)
    }

    // TODO: fix dates handling
    @Test
    fun `one test to rule them all`(): Unit = runBlocking {
        // 1 - Load and parse json file
        val jsonFile = Paths.get("src", "test", "resources", fileName).toFile()
        jsonFile shouldNotBe null
        val jsonString = FileReader(jsonFile).readText()
        jsonString.length shouldBeGreaterThan 0
        val json = Gson().fromJson<ExposureWindowsJsonInput>(jsonString, ExposureWindowsJsonInput::class.java)
        json shouldNotBe null

        // 2 - Check configuration and test cases
        checkConfiguration(json)
        debugLog("Configuration: checked", LogLevel.ALL)
        json.testCases.map { case -> checkTestCase(case) }
        debugLog("Test cases checked. Total count: ${json.testCases.size}", LogLevel.ALL)

        // 3 - Mock calculation configuration and create default risk level with it
        coEvery { appConfigProvider.getAppConfig() } returns json.defaultRiskCalculationConfiguration
        every { appConfigProvider.currentConfig } returns flow { configData }
        riskLevels = DefaultRiskLevels(appConfigProvider)

        // 4 - Mock and log exposure windows
        val allExposureWindows = mutableListOf<ExposureWindow>()
        for (case: TestCase in json.testCases) {
            val exposureWindows: List<ExposureWindow> =
                case.exposureWindows.map { window -> jsonToExposureWindow(window) }
            allExposureWindows.addAll(exposureWindows)

            // 5 - Calculate risk level for test case and aggregate results
            val exposureWindowsAndResult = HashMap<ExposureWindow, RiskResult>()
            for (exposureWindow: ExposureWindow in exposureWindows) {
                val riskResult = riskLevels.calculateRisk(exposureWindow) ?: continue
                exposureWindowsAndResult.put(exposureWindow, riskResult)
            }
            debugLog(
                "Exposure windows and result: ${exposureWindowsAndResult.size}",
                LogLevel.ALL
            )

            val aggregatedRiskResult = riskLevels.aggregateResults(exposureWindowsAndResult)

            debugLog(
                "\n" + comparisonDebugTable(aggregatedRiskResult, case),
                LogLevel.ONLY_COMPARISON
            )

            // 6 - Check with expected result from test case
//            aggregatedRiskResult.totalRiskLevel.number shouldBe case.expTotalRiskLevel
//            aggregatedRiskResult.mostRecentDateWithHighRisk shouldBe case.expAgeOfMostRecentDateWithHighRisk
//            aggregatedRiskResult.mostRecentDateWithLowRisk shouldBe case.expAgeOfMostRecentDateWithLowRisk
//            aggregatedRiskResult.totalMinimumDistinctEncountersWithHighRisk shouldBe case.expNumberOfExposureWindowsWithHighRisk
//            aggregatedRiskResult.totalMinimumDistinctEncountersWithLowRisk shouldBe case.expNumberOfExposureWindowsWithLowRisk
        }
    }

    private fun comparisonDebugTable(aggregated: AggregatedRiskResult, case: TestCase): String {
        val result = StringBuilder()
        result.append("\n").append("${case.description}")
        result.append("\n").append("+----------------------+-----------+-----------+")
        result.append("\n").append("| Property             | Expected  | Actual    |")
        result.append("\n").append("+----------------------+-----------+-----------+")
        result.append(
            addPropertyCheckToComparisonDebugTable(
                "Total Risk",
                aggregated.totalRiskLevel.number,
                case.expTotalRiskLevel
            )
        )
        result.append(
            addPropertyCheckToComparisonDebugTable(
                "Date With High Risk",
                aggregated.mostRecentDateWithHighRisk,
                case.expAgeOfMostRecentDateWithHighRisk
            )
        )
        result.append(
            addPropertyCheckToComparisonDebugTable(
                "Date With Low Risk",
                aggregated.mostRecentDateWithLowRisk,
                case.expAgeOfMostRecentDateWithLowRisk
            )
        )
        result.append(
            addPropertyCheckToComparisonDebugTable(
                "Encounters High Risk",
                aggregated.totalMinimumDistinctEncountersWithHighRisk,
                case.expNumberOfExposureWindowsWithHighRisk
            )
        )
        result.append(
            addPropertyCheckToComparisonDebugTable(
                "Encounters Low Risk",
                aggregated.totalMinimumDistinctEncountersWithLowRisk,
                case.expNumberOfExposureWindowsWithLowRisk
            )
        )
        return result.toString()
    }

    private fun addPropertyCheckToComparisonDebugTable(propertyName: String, expected: Any?, actual: Any?): String {
        val format = "| %-20s | %-9s | %-9s |"
        val result = StringBuilder()
        result.append("\n").append(String.format(format, propertyName, expected, actual))
        result.append("\n").append("+----------------------+-----------+-----------+")
        return result.toString()
    }

    private fun checkConfiguration(json: ExposureWindowsJsonInput) {
        json.defaultRiskCalculationConfiguration shouldNotBe null
        json.defaultRiskCalculationConfiguration.minutesAtAttenuationFilters.size shouldBeGreaterThan 0
        json.defaultRiskCalculationConfiguration.minutesAtAttenuationWeights.size shouldBeGreaterThan 0
        json.defaultRiskCalculationConfiguration.normalizedTimePerDayToRiskLevelMappingList.size shouldBeGreaterThan 0
        json.defaultRiskCalculationConfiguration.normalizedTimePerExposureWindowToRiskLevelMapping.size shouldBeGreaterThan 0
        json.defaultRiskCalculationConfiguration.transmissionRiskLevelMultiplier shouldNotBe null
        json.defaultRiskCalculationConfiguration.transmissionRiskLevelEncoding shouldNotBe null
        json.defaultRiskCalculationConfiguration.transmissionRiskLevelFilters.size shouldBeGreaterThan 0
    }

    private fun checkTestCase(case: TestCase) {
        debugLog("Checking ${case.description}", LogLevel.ALL)
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

    private fun jsonToExposureWindow(json: JsonWindow): ExposureWindow {
        val exposureWindow: ExposureWindow = mockk()

        every { exposureWindow.calibrationConfidence } returns json.calibrationConfidence
        every { exposureWindow.dateMillisSinceEpoch } returns (DateTimeConstants.MILLIS_PER_DAY * json.ageInDays).toLong()
        every { exposureWindow.infectiousness } returns json.infectiousness
        every { exposureWindow.reportType } returns json.reportType

        debugLog(
            "Mocking Exposure window: #%s\n" +
                "-Calibration Confidence: %s\n" +
                "-Date Millis Since Epoch: %s\n" +
                "-Infectiousness: %s\n" +
                "-Report type: %s".format(
                    exposureWindow.hashCode(),
                    exposureWindow.calibrationConfidence,
                    exposureWindow.dateMillisSinceEpoch,
                    exposureWindow.infectiousness,
                    exposureWindow.reportType
                ), LogLevel.ALL
        )

        every { exposureWindow.scanInstances } returns json.scanInstances.map { scanInstance ->
            jsonToScanInstance(
                scanInstance
            )
        }

        debugLog(
            "Mocking Exposure window: #%s\n" +
                "-Scan Instances count: %s\n".format(
                    exposureWindow.hashCode(),
                    exposureWindow.scanInstances.size
                ), LogLevel.ALL
        )

        return exposureWindow
    }

    private fun jsonToScanInstance(json: JsonScanInstance): ScanInstance {
        val scanInstance: ScanInstance = mockk()
        every { scanInstance.minAttenuationDb } returns json.minAttenuation
        every { scanInstance.secondsSinceLastScan } returns json.secondsSinceLastScan
        every { scanInstance.typicalAttenuationDb } returns json.typicalAttenuation

        debugLog(
            "-- Mocking Scan Instance: #%s\n" +
                "-Min Attenuation: %s\n" +
                "-Seconds Since Last Scan: %s\n" +
                "-Typical Attenuation:%s".format(
                    scanInstance.hashCode(),
                    scanInstance.minAttenuationDb,
                    scanInstance.secondsSinceLastScan,
                    scanInstance.typicalAttenuationDb
                ), LogLevel.ALL
        )

        return scanInstance
    }
}
