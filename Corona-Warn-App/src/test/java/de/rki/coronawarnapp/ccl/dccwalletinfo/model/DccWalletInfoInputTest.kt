package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty

internal class DccWalletInfoInputTest : BaseTest() {

    private val mapper = SerializationModule().jacksonObjectMapper()

    private val dccWalletInfoInput = DccWalletInfoInput(
        os = "android",
        language = "en",
        now = SystemTime(
            timestamp = 1640854800,
            localDate = "2021-12-30",
            localDateTime = "2021-12-30T10:00:00+01:00",
            localDateTimeMidnight = "2021-12-30T00:00:00+01:00",
            utcDate = "2021-12-30",
            utcDateTime = "2021-12-30T09:00:00Z",
            utcDateTimeMidnight = "2021-12-30T00:00:00Z"
        ),
        certificates = listOf(
            CclCertificate(
                barcodeData = "HC1:...",
                cose = Cose(kid = "IyG53x+1zj0="),
                cwt = Cwt(
                    iss = "DE",
                    iat = 1640691110,
                    exp = 1672227110
                ),
                hcert = mapper.valueToTree(
                    Hcert(
                        ver = "1.3.0",
                        nam = Nam(
                            fn = "Baxter",
                            gn = "Henrietta",
                            fnt = "BAXTER",
                            gnt = "HENRIETTA"
                        ),
                        dob = "1985-10-14",
                        v = mapper.readTree(
                            """
                            [
                                      {
                                        "ci": "URN:UVCI:01DE/IZSAP00A/3Y3DWEIPGJYQVFUXNQ2OWN#B",
                                        "co": "DE",
                                        "dn": 2,
                                        "dt": "2021-12-03",
                                        "is": "Robert Koch-Institut",
                                        "ma": "ORG-100031184",
                                        "mp": "EU/1/20/1507",
                                        "sd": 2,
                                        "tg": "840539006",
                                        "vp": "1119349007"
                                      }
                            ]
                        """.trimIndent()
                        )
                    )
                ),
                validityState = CclCertificate.Validity.VALID
            )
        ),
        boosterNotificationRules = readBoosterRules()
    )

    @Test
    fun `Deserialize DccWalletInfoInput`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_input.json").bufferedReader().use {
            mapper.readValue<DccWalletInfoInput>(it) shouldBe dccWalletInfoInput
        }
    }

    @Test
    fun `Serialize DccWalletInfoInput`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_input.json").bufferedReader().use {
            mapper.writeValueAsString(dccWalletInfoInput).toComparableJsonPretty() shouldBe
                it.readText().toComparableJsonPretty()
        }
    }

    private fun readBoosterRules(): JsonNode {
        return javaClass.classLoader!!.getResourceAsStream("ccl/dcc_booster_rules.json").bufferedReader()
            .use {
                mapper.readTree(it)
            }
    }
}

data class Nam(
    @JsonProperty("fn")
    val fn: String,

    @JsonProperty("gn")
    val gn: String,

    @JsonProperty("fnt")
    val fnt: String,

    @JsonProperty("gnt")
    val gnt: String
)

data class Hcert(
    @JsonProperty("ver")
    val ver: String,

    @JsonProperty("nam")
    val nam: Nam,

    @JsonProperty("dob")
    val dob: String,

    @JsonProperty("v")
    val v: JsonNode? = null,

    @JsonProperty("r")
    val r: JsonNode? = null,

    @JsonProperty("t")
    val t: JsonNode? = null
)
