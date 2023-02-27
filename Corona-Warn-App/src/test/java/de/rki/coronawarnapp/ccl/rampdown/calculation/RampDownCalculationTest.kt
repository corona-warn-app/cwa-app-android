package de.rki.coronawarnapp.ccl.rampdown.calculation

import com.fasterxml.jackson.databind.JsonNode
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CclJsonFunctions
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.ccl.rampdown.model.RampDownOutput
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.time.ZonedDateTime

internal class RampDownCalculationTest : BaseTest() {

    @MockK lateinit var cclFunctions: CclJsonFunctions
    private val mapper = SerializationModule.jacksonBaseMapper
    private val input = mapper.readTree(
        """
            {
            	"os": "android",
            	"language": "de",
            	"now": {
            		"timestamp": 1621496800,
            		"localDate": "2021-05-20",
            		"localDateTime": "2021-05-20T07:46:40Z",
            		"localDateTimeMidnight": "2021-05-20T00:00:00Z",
            		"utcDate": "2021-05-20",
            		"utcDateTime": "2021-05-20T07:46:40Z",
            		"utcDateTimeMidnight": "2021-05-20T00:00:00Z"
            	}
            }
        """.trimIndent()
    )

    private val output = mapper.readTree(
        """
                            {
                                "visible" : true,
                                "titleText" : {
                                  "type" : "string",
                                  "localizedText" : {
                                    "de" : "Some Status Title"
                                  },
                                  "parameters" : [ ]
                                },
                                "subtitleText" : {
                                  "type" : "string",
                                  "localizedText" : {
                                    "de" : "Some Status Subtitle"
                                  },
                                  "parameters" : [ ]
                                },
                                "longText" : {
                                  "type" : "string",
                                  "localizedText" : {
                                    "de" : "Some Status Long Text"
                                  },
                                  "parameters" : [ ]
                                },
                                "faqAnchor" : "ramp_down"
                            }
        """.trimIndent()
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { cclFunctions.evaluateFunction(any(), any()) } answers {
            args[0] shouldBe "getStatusTabNotice"
            (args[1] as JsonNode).asText() shouldBe input.asText()

            output
        }
    }

    @Test
    fun `getStatusTabNotice - visible`() = runTest {
        instance().getStatusTabNotice(ZonedDateTime.parse("2021-05-20T07:46:40Z")) shouldBe
            RampDownOutput(
                visible = true,
                titleText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "Some Status Title"),
                    parameters = emptyList()
                ),
                subtitleText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "Some Status Subtitle"),
                    parameters = emptyList()
                ),
                longText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "Some Status Long Text"),
                    parameters = emptyList()
                ),
                faqAnchor = "ramp_down"
            )
    }

    @Test
    fun `getStatusTabNotice - invisible`() = runTest {
        coEvery { cclFunctions.evaluateFunction(any(), any()) } returns mapper.readTree(
            """
                {
                    "visible" : false
                }
            """.trimIndent()
        )
        instance().getStatusTabNotice(ZonedDateTime.parse("2021-05-20T07:46:40Z")) shouldBe
            RampDownOutput(
                visible = false,
                titleText = null,
                subtitleText = null,
                longText = null,
                faqAnchor = null
            )
    }

    private fun instance() = RampDownCalculation(
        mapper = mapper,
        cclJsonFunctions = cclFunctions,
        dispatcherProvider = TestDispatcherProvider()
    )
}
