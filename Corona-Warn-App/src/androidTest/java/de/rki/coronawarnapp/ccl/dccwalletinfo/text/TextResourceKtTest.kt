package de.rki.coronawarnapp.ccl.dccwalletinfo.text

import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.Test
import testhelpers.BaseTestInstrumentation
import java.nio.file.Paths
import java.util.Locale

class TextResourceTest : BaseTestInstrumentation() {

    @Test
    fun runFormat() {

        val path = Paths.get("ccl", "ccl-text-descriptor-test-cases.gen.json").toString()
        val context = InstrumentationRegistry.getInstrumentation().context
        val stream = context.assets.open(path)
        val testCases = SerializationModule().jacksonObjectMapper().readValue<TestCases>(stream)

        testCases.testCases.forEach { testCase ->
            formatCCLText(
                ApplicationProvider.getApplicationContext(),
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


