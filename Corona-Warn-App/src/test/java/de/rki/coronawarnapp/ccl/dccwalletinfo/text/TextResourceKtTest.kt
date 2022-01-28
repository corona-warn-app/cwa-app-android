package de.rki.coronawarnapp.ccl.dccwalletinfo.text

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import java.util.Locale

internal class TextResourceKtTest : BaseTest() {
    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(FormatTestCasesProvider::class)
    fun run(testCase: TestCase) {
        val message = jacksonObjectMapper()
            .writeValueAsString(testCase.textDescriptor)
            .toComparableJsonPretty()

        println(testCase.description)
        println(message)
        println("Expected: " + testCase.assertions[0].text)

        formatCCLText(
            testCase.textDescriptor,
            Locale.GERMAN
        ) shouldBe testCase.assertions[0].text
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
