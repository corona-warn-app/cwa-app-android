package de.rki.coronawarnapp.util.serialization.validation

import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import okio.buffer
import okio.source
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class JsonSchemaValidatorTest : BaseTest() {

    private val schemaSource = object : JsonSchemaSource {
        override val rawSchema: String
            get() = this.javaClass.classLoader!!
                .getResourceAsStream("jsonschema-dcc-06fe7ed.json")
                .source()
                .buffer()
                .readUtf8()
        override val version: JsonSchemaSource.Version = JsonSchemaSource.Version.V2019_19
    }

    private val validJson = """
        {
            "ver": "1.2.1",
            "nam": {
                "fn": "Musterfrau-G\u00f6\u00dfinger",
                "gn": "Gabriele",
                "fnt": "MUSTERFRAU<GOESSINGER",
                "gnt": "GABRIELE"
            },
            "dob": "1998-02-26",
            "v": [
                {
                    "tg": "840539006",
                    "vp": "1119349007",
                    "mp": "EU\/1\/20\/1528",
                    "ma": "ORG-100030215",
                    "dn": 1,
                    "sd": 2,
                    "dt": "2021-02-18",
                    "co": "AT",
                    "is": "Ministry of Health, Austria",
                    "ci": "URN:UVCI:01:AT:10807843F94AEE0EE5093FBC254BD813#B"
                }
            ]
        }
    """.trimIndent()
    private val invalidJson = """
        {
            "ver": "1.2.1",
            "nam": {
                "fn": "Musterfrau-G\u00f6\u00dfinger",
                "gn": "Gabriele",
                "fnt": "MUSTERFRAU<GOESSINGER",
                "gnt": "GABRIELE"
            },
            "dob": "199",
            "v": [
                {
                    "tg": "840539006",
                    "vp": "1119349007",
                    "mp": "EU\/1\/20\/1528",
                    "ma": "ORG-100030215",
                    "dn": 1,
                    "sd": 10,
                    "co": "AT",
                    "is": "Ministry of Health, Austria",
                    "ci": "URN:UVCI:01:AT:10807843F94AEE0EE5093FBC254BD813#B"
                }
            ]
        }
    """.trimIndent()

    fun createInstance() = JsonSchemaValidator(SerializationModule().jacksonObjectMapper())

    @Test
    fun `simple pass`() {
        createInstance().validate(schemaSource, validJson).apply {
            isValid shouldBe true
        }
    }

    @Test
    fun `simple fail`() {
        createInstance().validate(schemaSource, invalidJson).apply {
            isValid shouldBe false
            errors.size shouldBe 3
        }
    }
}
