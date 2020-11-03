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
import io.kotest.matchers.shouldNotBe
import timber.log.Timber
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
        Timber.v(jsonFile.toString())
        val reader = FileReader(jsonFile).readText()

//        Timber.v(reader)
//        val json =  JsonReader().
//        Timber.v("Json: $json")
//        val json =  JsonReader(jsonFile.)
//        val json =  JsonReader(FileReader("exposure-windows-risk-calculation.json"))
//        val topic = gson.fromJson(json, Topic::class.java)
//        val topic = Gson().fromJson(json, ExposureWindowsJsonInput::class)
//        val gson = Gson()
//        val test = gson.fromJson<ExposureWindowsJsonInput>(json, ExposureWindowsJsonInput::class.java)
    }

}
