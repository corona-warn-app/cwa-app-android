package de.rki.coronawarnapp.ccl.holder.grouping

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.util.dcc.cleanHolderName
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTestInstrumentation

class DccHolderNameComponentsTest : BaseTestInstrumentation() {

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(DccHolderNameComponentsTestCaseProvider::class)
    fun allTestCases(testCase: TestCaseName) {
        val mapper = SerializationModule().jacksonObjectMapper()
        println("Executing TestCase: ${mapper.writerWithDefaultPrettyPrinter().writeValueAsString(testCase)}")

        testCase.name.cleanHolderName() shouldBe testCase.expectedResult
    }
}

data class TestCasesName(
    @JsonProperty("\$comment")
    val comment: String,

    @JsonProperty("\$sourceHash")
    val sourceHash: String,

    @JsonProperty("data")
    val testCases: List<TestCaseName>
)

data class TestCaseName(
    @JsonProperty("description")
    val description: String,

    @JsonProperty("actName")
    val name: String,

    @JsonProperty("expNameComponents")
    val expectedResult: List<String>
)

