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
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTestInstrumentation
import java.nio.file.Paths
import java.util.Locale
import java.util.TimeZone

class TextResourceTest : BaseTestInstrumentation() {

    @Before
    fun setup() {
        mockkObject(BuildVersionWrap)
        Locale.setDefault(Locale.GERMAN)

        val timeZone = TimeZone.getTimeZone("Europe/Berlin")
        TimeZone.setDefault(timeZone)
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(timeZone))
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
        val files = listOf(
            "ccl-text-descriptor-test-cases.gen.json",
            "ccl-text-descriptor-test-cases.gen2.json",
        )
        for (file in files) {
            val path = Paths.get("ccl", file).toString()
            val stream = InstrumentationRegistry.getInstrumentation().context.assets.open(path)
            val testCases = SerializationModule().jacksonObjectMapper().readValue<TestCases>(stream)
            testCases.testCases.forEach { testCase ->
                testCase.textDescriptor.format(
                    Locale.GERMAN,
                    Instant.parse("2022-01-31T00:00:00.000Z")
                ) shouldBe testCase.assertions[0].text
            }
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
