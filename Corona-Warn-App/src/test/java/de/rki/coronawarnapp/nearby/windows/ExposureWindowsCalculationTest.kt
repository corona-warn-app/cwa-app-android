package de.rki.coronawarnapp.nearby.windows

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import com.google.gson.Gson
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.nearby.windows.entities.ExposureWindowsJsonInput
import de.rki.coronawarnapp.nearby.windows.entities.cases.JsonScanInstance
import de.rki.coronawarnapp.nearby.windows.entities.cases.JsonWindow
import de.rki.coronawarnapp.nearby.windows.entities.cases.TestCase
import de.rki.coronawarnapp.nearby.windows.entities.configuration.DefaultRiskCalculationConfiguration
import de.rki.coronawarnapp.nearby.windows.entities.configuration.JsonMinutesAtAttenuationFilter
import de.rki.coronawarnapp.nearby.windows.entities.configuration.JsonMinutesAtAttenuationWeight
import de.rki.coronawarnapp.nearby.windows.entities.configuration.JsonNormalizedTimeToRiskLevelMapping
import de.rki.coronawarnapp.nearby.windows.entities.configuration.JsonTrlFilter
import de.rki.coronawarnapp.risk.DefaultRiskLevels
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.serialization.fromJson
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
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.io.FileReader
import java.nio.file.Paths

class ExposureWindowsCalculationTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var timeStamper: TimeStamper

    private lateinit var riskLevels: DefaultRiskLevels
    private lateinit var testConfig: ConfigData

    // Json file (located in /test/resources/exposure-windows-risk-calculation.json)
    private val fileName = "exposure-windows-risk-calculation.json"

    // Debug logs
    private enum class LogLevel {
        NONE,
        ONLY_COMPARISON,
        EXTENDED,
        ALL
    }

    private val logLevel = LogLevel.ONLY_COMPARISON

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.now()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun debugLog(s: String, toShow: LogLevel = LogLevel.ALL) {
        if (logLevel < toShow)
            return
        Timber.v(s)
    }

    @Test
    fun `one test to rule them all`(): Unit = runBlocking {
        // 1 - Load and parse json file
        val jsonFile = Paths.get("src", "test", "resources", fileName).toFile()
        jsonFile shouldNotBe null
        val jsonString = FileReader(jsonFile).readText()
        jsonString.length shouldBeGreaterThan 0
        val json = Gson().fromJson<ExposureWindowsJsonInput>(jsonString)
        json shouldNotBe null

        // 2 - Check test cases
        for (case: TestCase in json.testCases) {
            checkTestCase(case)
        }
        debugLog("Test cases checked. Total count: ${json.testCases.size}")

        // 3 - Mock calculation configuration and create default risk level with it
        setupTestConfiguration(json.defaultRiskCalculationConfiguration)
        coEvery { appConfigProvider.getAppConfig() } returns testConfig
        every { appConfigProvider.currentConfig } returns flow { testConfig }
        logConfiguration(testConfig)

        riskLevels = DefaultRiskLevels()

        val appConfig = appConfigProvider.getAppConfig()

        // 4 - Mock and log exposure windows
        val allExposureWindows = mutableListOf<ExposureWindow>()
        for (case: TestCase in json.testCases) {
            val exposureWindows: List<ExposureWindow> =
                case.exposureWindows.map { window -> jsonToExposureWindow(window) }
            allExposureWindows.addAll(exposureWindows)

            // 5 - Calculate risk level for test case and aggregate results
            val exposureWindowsAndResult = HashMap<ExposureWindow, RiskResult>()
            for (exposureWindow: ExposureWindow in exposureWindows) {

                logExposureWindow(exposureWindow, "➡➡ EXPOSURE WINDOW PASSED ➡➡", LogLevel.EXTENDED)
                val riskResult = riskLevels.calculateRisk(appConfig, exposureWindow) ?: continue
                exposureWindowsAndResult[exposureWindow] = riskResult
            }
            debugLog("Exposure windows and result: ${exposureWindowsAndResult.size}")

            val aggregatedRiskResult = riskLevels.aggregateResults(appConfig, exposureWindowsAndResult)

            debugLog(
                "\n" + comparisonDebugTable(aggregatedRiskResult, case),
                LogLevel.ONLY_COMPARISON
            )

            // 6 - Check with expected result from test case
            aggregatedRiskResult.totalRiskLevel.number shouldBe case.expTotalRiskLevel
            aggregatedRiskResult.mostRecentDateWithHighRisk shouldBe getTestCaseDate(case.expAgeOfMostRecentDateWithHighRiskInDays)
            aggregatedRiskResult.mostRecentDateWithLowRisk shouldBe getTestCaseDate(case.expAgeOfMostRecentDateWithLowRiskInDays)
            aggregatedRiskResult.totalMinimumDistinctEncountersWithHighRisk shouldBe case.expTotalMinimumDistinctEncountersWithHighRisk
            aggregatedRiskResult.totalMinimumDistinctEncountersWithLowRisk shouldBe case.expTotalMinimumDistinctEncountersWithLowRisk
        }
    }

    private fun getTestCaseDate(expAge: Long?): Instant? {
        if (expAge == null) return null
        return timeStamper.nowUTC - expAge * DateTimeConstants.MILLIS_PER_DAY
    }

    private fun comparisonDebugTable(aggregated: AggregatedRiskResult, case: TestCase): String {
        val result = StringBuilder()
        result.append("\n").append(case.description)
        result.append("\n").append("+----------------------+--------------------------+--------------------------+")
        result.append("\n").append("| Property             | Actual                   | Expected                 |")
        result.append("\n").append("+----------------------+--------------------------+--------------------------+")
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
                getTestCaseDate(case.expAgeOfMostRecentDateWithHighRiskInDays)
            )
        )
        result.append(
            addPropertyCheckToComparisonDebugTable(
                "Date With Low Risk",
                aggregated.mostRecentDateWithLowRisk,
                getTestCaseDate(case.expAgeOfMostRecentDateWithLowRiskInDays)
            )
        )
        result.append(
            addPropertyCheckToComparisonDebugTable(
                "Encounters High Risk",
                aggregated.totalMinimumDistinctEncountersWithHighRisk,
                case.expTotalMinimumDistinctEncountersWithHighRisk
            )
        )
        result.append(
            addPropertyCheckToComparisonDebugTable(
                "Encounters Low Risk",
                aggregated.totalMinimumDistinctEncountersWithLowRisk,
                case.expTotalMinimumDistinctEncountersWithLowRisk
            )
        )
        result.append("\n")
        return result.toString()
    }

    private fun addPropertyCheckToComparisonDebugTable(propertyName: String, expected: Any?, actual: Any?): String {
        val format = "| %-20s | %-24s | %-24s |"
        val result = StringBuilder()
        result.append("\n").append(String.format(format, propertyName, expected, actual))
        result.append("\n").append("+----------------------+--------------------------+--------------------------+")
        return result.toString()
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

    private fun logConfiguration(config: ConfigData) {
        val result = StringBuilder()
        result.append("\n\n").append("----------------- \uD83D\uDEE0 CONFIGURATION \uD83D\uDEE0 -----------")

        result.append("\n").append("◦ Minutes At Attenuation Filters (${config.minutesAtAttenuationFilters.size})")
        for (filter: RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter in config.minutesAtAttenuationFilters) {
            result.append("\n\t").append("⇥ Filter")
            result.append(logRange(filter.attenuationRange, "Attenuation Range"))
            result.append(logRange(filter.dropIfMinutesInRange, "Drop If Minutes In Range"))
        }

        result.append("\n").append("◦ Minutes At Attenuation Weights (${config.minutesAtAttenuationWeights.size})")
        for (weight: RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight in config.minutesAtAttenuationWeights) {
            result.append("\n\t").append("⇥ Weight")
            result.append(logRange(weight.attenuationRange, "Attenuation Range"))
            result.append("\n\t\t").append("↳ Weight: ${weight.weight}")
        }

        result.append("\n")
            .append("◦ Normalized Time Per Day To Risk Level Mapping List (${config.normalizedTimePerDayToRiskLevelMappingList.size})")
        for (mapping: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping in config.normalizedTimePerDayToRiskLevelMappingList) {
            result.append("\n\t").append("⇥ Mapping")
            result.append(logRange(mapping.normalizedTimeRange, "Normalized Time Range"))
            result.append("\n\t\t").append("↳ Risk Level: ${mapping.riskLevel}")
        }

        result.append("\n")
            .append("◦ Normalized Time Per Exposure Window To Risk Level Mapping (${config.normalizedTimePerExposureWindowToRiskLevelMapping.size})")
        for (mapping: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping in config.normalizedTimePerExposureWindowToRiskLevelMapping) {
            result.append("\n\t").append("⇥ Mapping")
            result.append(logRange(mapping.normalizedTimeRange, "Normalized Time Range"))
            result.append("\n\t\t").append("↳ Risk Level: ${mapping.riskLevel}")
        }

        result.append("\n").append("◦ Transmission Risk Level Encoding:")
        result.append("\n\t")
            .append("↳ Infectiousness Offset High: ${config.transmissionRiskLevelEncoding.infectiousnessOffsetHigh}")
        result.append("\n\t")
            .append("↳ Infectiousness Offset Standard: ${config.transmissionRiskLevelEncoding.infectiousnessOffsetStandard}")
        result.append("\n\t")
            .append("↳ Report Type Offset Confirmed Clinical Diagnosis: ${config.transmissionRiskLevelEncoding.reportTypeOffsetConfirmedClinicalDiagnosis}")
        result.append("\n\t")
            .append("↳ Report Type Offset Confirmed Test: ${config.transmissionRiskLevelEncoding.reportTypeOffsetConfirmedTest}")
        result.append("\n\t")
            .append("↳ Report Type Offset Recursive: ${config.transmissionRiskLevelEncoding.reportTypeOffsetRecursive}")
        result.append("\n\t")
            .append("↳ Report Type Offset Self Report: ${config.transmissionRiskLevelEncoding.reportTypeOffsetSelfReport}")

        result.append("\n").append("◦ Transmission Risk Level Filters (${config.transmissionRiskLevelFilters.size})")
        for (filter: RiskCalculationParametersOuterClass.TrlFilter in config.transmissionRiskLevelFilters) {
            result.append("\n\t").append("⇥ Trl Filter")
            result.append(logRange(filter.dropIfTrlInRange, "Drop If Trl In Range"))
        }

        result.append("\n").append("◦ Transmission Risk Level Multiplier: ${config.transmissionRiskLevelMultiplier}")
        result.append("\n").append("-------------------------------------------- ⚙ -").append("\n")
        debugLog(result.toString(), LogLevel.NONE)
    }

    private fun logRange(range: RiskCalculationParametersOuterClass.Range, rangeName: String): String {
        val builder = StringBuilder()
        builder.append("\n\t\t").append("⇥ $rangeName")
        builder.append("\n\t\t\t").append("↳ Min: ${range.min}")
        builder.append("\n\t\t\t").append("↳ Max: ${range.max}")
        builder.append("\n\t\t\t").append("↳ Min Exclusive: ${range.minExclusive}")
        builder.append("\n\t\t\t").append("↳ Max Exclusive: ${range.maxExclusive}")
        return builder.toString()
    }

    private fun logExposureWindow(exposureWindow: ExposureWindow, title: String, logLevel: LogLevel = LogLevel.ALL) {
        val result = StringBuilder()
        result.append("\n\n").append("------------ $title -----------")
        result.append("\n").append("Mocked Exposure window: #${exposureWindow.hashCode()}")
        result.append("\n").append("◦ Calibration Confidence: ${exposureWindow.calibrationConfidence}")
        result.append("\n").append("◦ Date Millis Since Epoch: ${exposureWindow.dateMillisSinceEpoch}")
        result.append("\n").append("◦ Infectiousness: ${exposureWindow.infectiousness}")
        result.append("\n").append("◦ Report type: ${exposureWindow.reportType}")

        result.append("\n").append("‣ Scan Instances (${exposureWindow.scanInstances.size}):")
        for (scan: ScanInstance in exposureWindow.scanInstances) {
            result.append("\n\t").append("⇥ Mocked Scan Instance: #${scan.hashCode()}")
            result.append("\n\t\t").append("↳ Min Attenuation: ${scan.minAttenuationDb}")
            result.append("\n\t\t").append("↳ Seconds Since Last Scan: ${scan.secondsSinceLastScan}")
            result.append("\n\t\t").append("↳ Typical Attenuation: ${scan.typicalAttenuationDb}")
        }
        result.append("\n").append("-------------------------------------------- ✂ ----").append("\n")
        debugLog(result.toString(), logLevel)
    }

    private fun setupTestConfiguration(json: DefaultRiskCalculationConfiguration) {

        testConfig = ConfigDataContainer(
            serverTime = Instant.now(),
            cacheValidity = Duration.standardMinutes(5),
            localOffset = Duration.ZERO,
            mappedConfig = configData,
            identifier = "soup",
            configType = ConfigData.Type.FROM_SERVER
        )

        val attenuationFilters = mutableListOf<RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter>()
        for (jsonFilter: JsonMinutesAtAttenuationFilter in json.minutesAtAttenuationFilters) {
            val filter: RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter = mockk()
            every { filter.attenuationRange.min } returns jsonFilter.attenuationRange.min
            every { filter.attenuationRange.max } returns jsonFilter.attenuationRange.max
            every { filter.attenuationRange.minExclusive } returns jsonFilter.attenuationRange.minExclusive
            every { filter.attenuationRange.maxExclusive } returns jsonFilter.attenuationRange.maxExclusive
            every { filter.dropIfMinutesInRange.min } returns jsonFilter.dropIfMinutesInRange.min
            every { filter.dropIfMinutesInRange.max } returns jsonFilter.dropIfMinutesInRange.max
            every { filter.dropIfMinutesInRange.minExclusive } returns jsonFilter.dropIfMinutesInRange.minExclusive
            every { filter.dropIfMinutesInRange.maxExclusive } returns jsonFilter.dropIfMinutesInRange.maxExclusive
            attenuationFilters.add(filter)
        }
        every { testConfig.minutesAtAttenuationFilters } returns attenuationFilters

        val attenuationWeights = mutableListOf<RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight>()
        for (jsonWeight: JsonMinutesAtAttenuationWeight in json.minutesAtAttenuationWeights) {
            val weight: RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight = mockk()
            every { weight.attenuationRange.min } returns jsonWeight.attenuationRange.min
            every { weight.attenuationRange.max } returns jsonWeight.attenuationRange.max
            every { weight.attenuationRange.minExclusive } returns jsonWeight.attenuationRange.minExclusive
            every { weight.attenuationRange.maxExclusive } returns jsonWeight.attenuationRange.maxExclusive
            every { weight.weight } returns jsonWeight.weight
            attenuationWeights.add(weight)
        }
        every { testConfig.minutesAtAttenuationWeights } returns attenuationWeights

        val normalizedTimePerDayToRiskLevelMapping =
            mutableListOf<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>()
        for (jsonMapping: JsonNormalizedTimeToRiskLevelMapping in json.normalizedTimePerDayToRiskLevelMapping) {
            val mapping: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping = mockk()
            every { mapping.riskLevel } returns RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.forNumber(
                jsonMapping.riskLevel
            )
            every { mapping.normalizedTimeRange.min } returns jsonMapping.normalizedTimeRange.min
            every { mapping.normalizedTimeRange.max } returns jsonMapping.normalizedTimeRange.max
            every { mapping.normalizedTimeRange.minExclusive } returns jsonMapping.normalizedTimeRange.minExclusive
            every { mapping.normalizedTimeRange.maxExclusive } returns jsonMapping.normalizedTimeRange.maxExclusive
            normalizedTimePerDayToRiskLevelMapping.add(mapping)
        }
        every { testConfig.normalizedTimePerDayToRiskLevelMappingList } returns normalizedTimePerDayToRiskLevelMapping

        val normalizedTimePerExposureWindowToRiskLevelMapping =
            mutableListOf<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>()
        for (jsonMapping: JsonNormalizedTimeToRiskLevelMapping in json.normalizedTimePerEWToRiskLevelMapping) {
            val mapping: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping = mockk()
            every { mapping.riskLevel } returns RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.forNumber(
                jsonMapping.riskLevel
            )
            every { mapping.normalizedTimeRange.min } returns jsonMapping.normalizedTimeRange.min
            every { mapping.normalizedTimeRange.max } returns jsonMapping.normalizedTimeRange.max
            every { mapping.normalizedTimeRange.minExclusive } returns jsonMapping.normalizedTimeRange.minExclusive
            every { mapping.normalizedTimeRange.maxExclusive } returns jsonMapping.normalizedTimeRange.maxExclusive
            normalizedTimePerExposureWindowToRiskLevelMapping.add(mapping)
        }
        every { testConfig.normalizedTimePerExposureWindowToRiskLevelMapping } returns normalizedTimePerExposureWindowToRiskLevelMapping

        every { testConfig.transmissionRiskLevelMultiplier } returns json.transmissionRiskLevelMultiplier

        val trlEncoding: RiskCalculationParametersOuterClass.TransmissionRiskLevelEncoding = mockk()
        every { trlEncoding.infectiousnessOffsetHigh } returns json.trlEncoding.infectiousnessOffsetHigh
        every { trlEncoding.infectiousnessOffsetStandard } returns json.trlEncoding.infectiousnessOffsetStandard
        every { trlEncoding.reportTypeOffsetConfirmedClinicalDiagnosis } returns json.trlEncoding.reportTypeOffsetConfirmedClinicalDiagnosis
        every { trlEncoding.reportTypeOffsetConfirmedTest } returns json.trlEncoding.reportTypeOffsetConfirmedTest
        every { trlEncoding.reportTypeOffsetRecursive } returns json.trlEncoding.reportTypeOffsetRecursive
        every { trlEncoding.reportTypeOffsetSelfReport } returns json.trlEncoding.reportTypeOffsetSelfReport
        every { testConfig.transmissionRiskLevelEncoding } returns trlEncoding

        val trlFilters = mutableListOf<RiskCalculationParametersOuterClass.TrlFilter>()
        for (jsonFilter: JsonTrlFilter in json.trlFilters) {
            val filter: RiskCalculationParametersOuterClass.TrlFilter = mockk()
            every { filter.dropIfTrlInRange.min } returns jsonFilter.dropIfTrlInRange.min
            every { filter.dropIfTrlInRange.max } returns jsonFilter.dropIfTrlInRange.max
            every { filter.dropIfTrlInRange.minExclusive } returns jsonFilter.dropIfTrlInRange.minExclusive
            every { filter.dropIfTrlInRange.maxExclusive } returns jsonFilter.dropIfTrlInRange.maxExclusive
            trlFilters.add(filter)
        }
        every { testConfig.transmissionRiskLevelFilters } returns trlFilters
    }

    private fun jsonToExposureWindow(json: JsonWindow): ExposureWindow {
        val exposureWindow: ExposureWindow = mockk()

        every { exposureWindow.calibrationConfidence } returns json.calibrationConfidence
        every { exposureWindow.dateMillisSinceEpoch } returns timeStamper.nowUTC.millis - (DateTimeConstants.MILLIS_PER_DAY * json.ageInDays).toLong()
        every { exposureWindow.infectiousness } returns json.infectiousness
        every { exposureWindow.reportType } returns json.reportType
        every { exposureWindow.scanInstances } returns json.scanInstances.map { scanInstance ->
            jsonToScanInstance(
                scanInstance
            )
        }

        logExposureWindow(exposureWindow, "⊞ EXPOSURE WINDOW MOCK ⊞")

        return exposureWindow
    }

    private fun jsonToScanInstance(json: JsonScanInstance): ScanInstance {
        val scanInstance: ScanInstance = mockk()
        every { scanInstance.minAttenuationDb } returns json.minAttenuation
        every { scanInstance.secondsSinceLastScan } returns json.secondsSinceLastScan
        every { scanInstance.typicalAttenuationDb } returns json.typicalAttenuation
        return scanInstance
    }
}
