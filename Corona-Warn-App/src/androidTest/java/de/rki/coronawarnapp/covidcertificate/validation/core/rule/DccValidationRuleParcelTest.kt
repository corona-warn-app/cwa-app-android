package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import android.os.Bundle
import android.os.Parcel
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.Test
import testhelpers.BaseUITest
import java.time.ZonedDateTime

class DccValidationRuleParcelTest : BaseUITest() {

    @Test
    fun parcelization() {
        val dccValidationRule = DccValidationRule(
            identifier = "identifier",
            typeDcc = DccValidationRule.Type.ACCEPTANCE,
            version = "1.0.0",
            schemaVersion = "1.0.0",
            engine = "engine",
            engineVersion = "1.0.0",
            certificateType = "general",
            description = listOf(DccValidationRule.Description(languageCode = "en", description = "text")),
            validFrom = ZonedDateTime.parse("2021-05-20T07:46:40Z").toString(),
            validTo = ZonedDateTime.parse("2021-05-27T07:46:40Z").toString(),
            affectedFields = listOf("aField"),
            logic = SerializationModule.jacksonBaseMapper.readTree(
                """
                    {
                        "and": [
                            {
                                ">": [
                                    {
                                        "var": "hcert.v.0.dn"
                                    },
                                    0
                                ]
                            },
                            {
                                ">=": [
                                    {
                                        "var": "hcert.v.0.dn"
                                    },
                                    {
                                        "var": "hcert.v.0.sd"
                                    }
                                ]
                            }
                        ]
                    }
                """.trimIndent()
            ),
            country = "de",
        )

        val bundle = Bundle().apply {
            putParcelable("dccValidationRule", dccValidationRule)
        }

        val parcelRaw = Parcel.obtain().apply {
            writeBundle(bundle)
        }.marshall()

        val restoredParcel = Parcel.obtain().apply {
            unmarshall(parcelRaw, 0, parcelRaw.size)
            setDataPosition(0)
        }

        val restoredData = restoredParcel.readBundle()!!.run {
            classLoader = DccValidationRule::class.java.classLoader
            getParcelable<DccValidationRule>("dccValidationRule")
        }
        restoredData shouldBe dccValidationRule
    }
}
