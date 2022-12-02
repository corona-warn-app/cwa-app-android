package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import com.google.gson.JsonSyntaxException
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.BaseTest

class DccValidationRuleConverterTest : BaseTest() {

    private val serializationModule = SerializationModule()
    private val mapper = serializationModule.jacksonObjectMapper()
    private val converter = DccValidationRuleConverter(mapper)

    private val testBoosterNotificationRulesData =
        """
            [
                {
                    "Type": "BoosterNotification",
                    "Logic": {
                        "!": [
                            {
                                "var": "payload.v.1"
                            }
                        ]
                    },
                    "Engine": "CERTLOGIC",
                    "Country": "LT",
                    "ValidTo": "2023-07-04T00:00:00Z",
                    "Version": "1.0.0",
                    "ValidFrom": "2021-07-04T15:00:00Z",
                    "Identifier": "TestIdentifier",
                    "Description": [
                        {
                            "desc": "One type of event of vaccination",
                            "lang": "en"
                        }
                    ],
                    "EngineVersion": "1.0.0",
                    "SchemaVersion": "1.0.0",
                    "AffectedFields": [
                        "v.1"
                    ],
                    "CertificateType": "Vaccination"
                }
            ]
        """.trimIndent()

    private val testAcceptanceRulesData =
        """
            [
                {
                    "Type": "Acceptance",
                    "Logic": {
                        "!": [
                            {
                                "var": "payload.v.1"
                            }
                        ]
                    },
                    "Engine": "CERTLOGIC",
                    "Country": "LT",
                    "ValidTo": "2023-07-04T00:00:00Z",
                    "Version": "1.0.0",
                    "ValidFrom": "2021-07-04T15:00:00Z",
                    "Identifier": "TestIdentifier",
                    "Description": [
                        {
                            "desc": "One type of event of vaccination",
                            "lang": "en"
                        }
                    ],
                    "EngineVersion": "1.0.0",
                    "SchemaVersion": "1.0.0",
                    "AffectedFields": [
                        "v.1"
                    ],
                    "CertificateType": "Vaccination"
                }
            ]
        """.trimIndent()

    private val testInvalidationRulesData =
        """
            [
                {
                    "Type": "Invalidation",
                    "Logic": {
                        "!": [
                            {
                                "var": "payload.v.1"
                            }
                        ]
                    },
                    "Engine": "CERTLOGIC",
                    "Country": "LT",
                    "ValidTo": "2023-07-04T00:00:00Z",
                    "Version": "1.0.0",
                    "ValidFrom": "2021-07-04T15:00:00Z",
                    "Identifier": "TestIdentifier",
                    "Description": [
                        {
                            "desc": "One type of event of vaccination",
                            "lang": "en"
                        }
                    ],
                    "EngineVersion": "1.0.0",
                    "SchemaVersion": "1.0.0",
                    "AffectedFields": [
                        "v.1"
                    ],
                    "CertificateType": "Vaccination"
                }
            ]
        """.trimIndent()

    private val testBoosterNotificationRule = DccValidationRule(
        identifier = "TestIdentifier",
        typeDcc = DccValidationRule.Type.BOOSTER_NOTIFICATION,
        country = "LT",
        version = "1.0.0",
        schemaVersion = "1.0.0",
        engine = "CERTLOGIC",
        engineVersion = "1.0.0",
        certificateType = "Vaccination",
        description = listOf(DccValidationRule.Description("en", "One type of event of vaccination")),
        validFrom = "2021-07-04T15:00:00Z",
        validTo = "2023-07-04T00:00:00Z",
        affectedFields = listOf("v.1"),
        logic = mapper.readTree("{\"!\":[{\"var\":\"payload.v.1\"}]}")
    )

    private val testInvalidationRule = testBoosterNotificationRule.copy(typeDcc = DccValidationRule.Type.INVALIDATION)

    private val testAcceptanceRule = testBoosterNotificationRule.copy(typeDcc = DccValidationRule.Type.ACCEPTANCE)

    @Test
    fun `returns empty list if input is null`() {
        converter.jsonToRuleSet(null) shouldBe emptyList()
    }

    @Test
    fun `converts all rule types`() {
        converter.run {
            jsonToRuleSet(testAcceptanceRulesData) shouldBe listOf(testAcceptanceRule)
            jsonToRuleSet(testBoosterNotificationRulesData) shouldBe listOf(testBoosterNotificationRule)
            jsonToRuleSet(testInvalidationRulesData) shouldBe listOf(testInvalidationRule)
        }
    }

    @Test
    fun `throws if input is invalid`() {
        val missingAttributes = """
            [
                {
                    "Type": "Acceptance",
                    "Engine": "CERTLOGIC",
                    "Country": "LT",
                    "Version": "1.0.0",
                    "Identifier": "VR-LT-0000",
                    "EngineVersion": "1.0.0",
                    "SchemaVersion": "1.0.0",
                }
            ]
        """.trimIndent()

        converter.run {
            assertThrows<JsonSyntaxException> { jsonToRuleSet(missingAttributes) }
            assertThrows<JsonSyntaxException> { jsonToRuleSet("abc123") }
        }
    }
}
