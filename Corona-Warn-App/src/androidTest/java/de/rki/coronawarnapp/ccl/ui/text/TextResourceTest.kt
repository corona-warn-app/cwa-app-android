package de.rki.coronawarnapp.ccl.ui.text

import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTestInstrumentation
import java.nio.file.Paths
import java.util.Locale

class TextResourceTest : BaseTestInstrumentation() {

    @Before
    fun setup() {
        mockkObject(BuildVersionWrap)
    }

    @Test
    fun runFormat24() {
        every { BuildVersionWrap.SDK_INT } returns 24
        testCases()
    }

    @Test
    fun runFormat23() {
        every { BuildVersionWrap.SDK_INT } returns 23
        testCases()
    }

    private fun testCases() {
        val path = Paths.get("ccl", "ccl-text-descriptor-test-cases.gen.json").toString()
        val context = InstrumentationRegistry.getInstrumentation().context
        val stream = context.assets.open(path)
        val testCases = SerializationModule().jacksonObjectMapper().readValue<TestCases>(stream)

        testCases.testCases.forEach { testCase ->
            formatCCLText(
                testCase.textDescriptor,
                Locale.GERMAN
            ) shouldBe testCase.assertions[0].text
        }
    }
}

data class TestCase(
    @JsonProperty("description")
    val description: String,

    @JsonProperty("textDescriptor")
    val textDescriptor: CCLText,

    @JsonProperty("assertions")
    val assertions: List<Assertions>
)

data class Assertions(
    @JsonProperty("languageCode")
    val languageCode: String,

    @JsonProperty("text")
    val text: String
)

data class TestCases(
    @JsonProperty("\$comment")
    val comment: String,
    @JsonProperty("testCases")
    val testCases: List<TestCase>
)
