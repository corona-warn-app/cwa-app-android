package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import com.fasterxml.jackson.databind.JsonNode
import de.rki.coronawarnapp.util.serialization.SerializationModule
import dgca.verifier.app.engine.UTC_ZONE_ID
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.ZonedDateTime

class DccValidationRuleTest : BaseTest() {

    fun createOne(
        identifier: String = "identifier",
        typeDcc: DccValidationRule.Type = DccValidationRule.Type.ACCEPTANCE,
        version: String = "1.0.0",
        schemaVersion: String = "1.0.0",
        engine: String = "engine",
        engineVersion: String = "1.0.0",
        certificateType: String = "general",
        description: Map<String, String> = emptyMap(),
        validFrom: String = ZonedDateTime.parse(
            "2021-07-09T11:22:22.378242Z"
        ).withZoneSameInstant(UTC_ZONE_ID).toString(),
        validTo: String = ZonedDateTime.parse(
            "2021-07-09T11:22:22.623499Z"
        ).withZoneSameInstant(UTC_ZONE_ID).toString(),
        affectedFields: List<String> = listOf("aField"),
        logic: JsonNode = SerializationModule.jacksonBaseMapper.readTree(
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
        country: String = "de",
    ) = DccValidationRule(
        identifier = identifier,
        typeDcc = typeDcc,
        version = version,
        schemaVersion = schemaVersion,
        engine = engine,
        engineVersion = engineVersion,
        certificateType = certificateType,
        description = description,
        validFrom = validFrom,
        validTo = validTo,
        affectedFields = affectedFields,
        logic = logic,
        country = country,
    )

    @Test
    fun `version comparison`() {
        val rules = listOf(
            createOne(identifier = "R-1", version = "1.0.0"),
            createOne(identifier = "R-2", version = "1.2.0"),
            createOne(identifier = "R-3", version = "1.3.0"),
            createOne(identifier = "R-2", version = "1.1.0"),
            createOne(identifier = "R-2", version = "1.0.42"),
            createOne(identifier = "R-3", version = "1.4.0"),
        )

        rules
            .groupBy { it.identifier }
            .map { entry ->
                entry.value.maxByOrNull { it.versionSemVer }
            } shouldBe listOf(
            createOne(identifier = "R-1", version = "1.0.0"),
            createOne(identifier = "R-2", version = "1.2.0"),
            createOne(identifier = "R-3", version = "1.4.0"),
        )
    }
}
