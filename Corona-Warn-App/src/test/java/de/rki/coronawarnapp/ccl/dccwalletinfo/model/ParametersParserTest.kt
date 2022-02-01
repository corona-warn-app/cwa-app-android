package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty

internal class ParametersParserTest : BaseTest() {
    private val mapper = ObjectMapper()

    private val parameters = listOf(
        Parameters(
            type = Parameters.Type.NUMBER,
            value = 5.5
        ),
        Parameters(
            type = Parameters.Type.BOOLEAN,
            value = true
        ),
        Parameters(
            type = Parameters.Type.STRING,
            value = "2G"
        ),
        Parameters(
            type = Parameters.Type.LOCAL_DATE,
            value = "2022-01-01T23:30:00.000Z"
        ),
        Parameters(
            type = Parameters.Type.LOCAL_DATE_TIME,
            value = "2022-01-01T23:30:00.000Z"
        ),
        Parameters(
            type = Parameters.Type.UTC_DATE,
            value = "2022-01-01T23:30:00.000Z"
        ),
        Parameters(
            type = Parameters.Type.UTC_DATE_TIME,
            value = "2022-01-01T23:30:00.000Z"
        )
    )

    @Test
    fun `Deserialize Parameters`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_params.json").use {
            mapper.readValue<List<Parameters>>(it) shouldBe parameters
        }
    }

    @Test
    fun `Serialize Parameters`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_params.json")
            .bufferedReader().use {
                mapper.writeValueAsString(parameters).toComparableJsonPretty() shouldBe
                    it.readText().toComparableJsonPretty()
            }
    }
}
