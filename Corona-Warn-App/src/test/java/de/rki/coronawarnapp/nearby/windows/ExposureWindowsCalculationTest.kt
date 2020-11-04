package de.rki.coronawarnapp.nearby.windows

import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.FileReader
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import de.rki.coronawarnapp.nearby.windows.entities.ExposureWindowsJsonInput
import de.rki.coronawarnapp.nearby.windows.entities.cases.ExposureWindow
import de.rki.coronawarnapp.nearby.windows.entities.cases.ScanInstance
import de.rki.coronawarnapp.nearby.windows.entities.cases.TestCase
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import org.mockito.internal.matchers.InstanceOf
import timber.log.Timber
import java.io.IOException
import java.nio.file.Paths

class ExposureWindowsCalculationTest: BaseTest() {

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

        json.testCases.map { case -> checkTestCase(case) }

        Timber.v("Test cases checked. Total count: ${json.testCases.size}")
    }

    private fun checkTestCase(case: TestCase) {
        Timber.v("Checking ${case.description}")

        case.expTotalRiskLevel shouldNotBe null
        case.expTotalMinimumDistinctEncountersWithLowRisk shouldNotBe null
        case.expTotalMinimumDistinctEncountersWithHighRisk shouldNotBe null

        case.exposureWindows.map { exposureWindow -> checkExposureWindow(exposureWindow) }
    }

    private fun checkExposureWindow(exposureWindow: ExposureWindow) {
        exposureWindow.ageInDays shouldNotBe null
        exposureWindow.reportType shouldNotBe null
        exposureWindow.infectiousness shouldNotBe null
        exposureWindow.calibrationConfidence shouldNotBe null
    }

}
