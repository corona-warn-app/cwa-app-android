package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty

@Suppress("MaxLineLength")
internal class DccWalletInfoParserTest : BaseTest() {

    private val mapper = SerializationModule.jacksonBaseMapper

    @Test
    fun `Deserialize DCCWalletInfo`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_output.json").use {
            mapper.readValue<DccWalletInfo>(it) shouldBe dccWalletInfo
        }
    }

    @Test
    fun `Serialize DCCWalletInfo`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_output.json").bufferedReader().use {
            mapper.writeValueAsString(dccWalletInfo).toComparableJsonPretty() shouldBe
                it.readText().toComparableJsonPretty()
        }
    }

    @Test
    fun `Deserialize DCCWalletInfo with Reissuance`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_output_with_reissuance.json").use {
            mapper.readValue<DccWalletInfo>(it) shouldBe dccWalletInfoWithReissuance
        }
    }

    @Test
    fun `Serialize DCCWalletInfo with Reissuance`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_output_with_reissuance.json").bufferedReader()
            .use {
                mapper.writeValueAsString(dccWalletInfoWithReissuance).toComparableJsonPretty() shouldBe
                    it.readText().toComparableJsonPretty()
            }
    }

    @Test
    fun `Deserialize DCCWalletInfo with Reissuance legacy`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_output_with_reissuance_legacy.json").use {
            mapper.readValue<DccWalletInfo>(it) shouldBe dccWalletInfoWithReissuanceLegacy
        }
    }

    @Test
    fun `Serialize DCCWalletInfo with Reissuance legacy`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_output_with_reissuance_legacy.json").bufferedReader()
            .use {
                mapper.writeValueAsString(dccWalletInfoWithReissuanceLegacy).toComparableJsonPretty() shouldBe
                    it.readText().toComparableJsonPretty()
            }
    }

    private val pluralTextIndexed = PluralText(
        type = "plural",
        quantity = null,
        quantityParameterIndex = 0,
        localizedText = mapOf(
            "en" to QuantityText(
                zero = "No time left",
                one = "%u minute left",
                two = "%u minutes left",
                few = "%u minutes left",
                many = "%u minutes left",
                other = "%u minutes left"
            )
        ),
        parameters = listOf(
            Parameters(
                type = Parameters.Type.NUMBER,
                value = 5.5
            )
        )
    )
    private val dccWalletInfoPluralIndexed = dccWalletInfo.copy(
        vaccinationState = dccWalletInfo.vaccinationState.copy(
            subtitleText = pluralTextIndexed
        )
    )

    @Test
    fun `Deserialize DCCWalletInfo - Plural Indexed`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_plural_indexed.json").use {
            mapper.readValue<DccWalletInfo>(it) shouldBe dccWalletInfoPluralIndexed
        }
    }

    @Test
    fun `Serialize DCCWalletInfo - Plural Indexed`() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info_plural_indexed.json")
            .bufferedReader().use {
                mapper.writeValueAsString(dccWalletInfoPluralIndexed).toComparableJsonPretty() shouldBe
                    it.readText().toComparableJsonPretty()
            }
    }

    @Test
    fun `Deserialize DCCWalletInfo - CertificatesRevokedByInvalidationRules`() {
        javaClass.classLoader!!.getResourceAsStream(certificatesRevokedByInvalidationRulesPath).use {
            mapper.readValue<DccWalletInfo>(it) shouldBe dccWalletInfoWithCertificatesRevokedByInvalidationRules
        }
    }

    @Test
    fun `Serialize DCCWalletInfo - CertificatesRevokedByInvalidationRules`() {
        javaClass.classLoader!!.getResourceAsStream(certificatesRevokedByInvalidationRulesPath).bufferedReader().use {
            mapper.writeValueAsString(dccWalletInfoWithCertificatesRevokedByInvalidationRules)
                .toComparableJsonPretty() shouldBe it.readText().toComparableJsonPretty()
        }
    }
}

private const val certificatesRevokedByInvalidationRulesPath =
    "ccl/dcc_wallet_info_output_with_certificatesRevokedByInvalidationRules.json"
