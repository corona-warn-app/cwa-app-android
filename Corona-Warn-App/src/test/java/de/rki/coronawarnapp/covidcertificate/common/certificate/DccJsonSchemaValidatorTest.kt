package de.rki.coronawarnapp.covidcertificate.common.certificate

import android.content.res.AssetManager
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaValidator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccJsonSchemaValidatorTest : BaseTest() {

    @MockK lateinit var assetManager: AssetManager
    @MockK lateinit var schemaValidator: JsonSchemaValidator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { assetManager.open(any()) } answers {
            this.javaClass.classLoader!!.getResourceAsStream(arg<String>(0))
        }

        every { schemaValidator.validate(any(), any()) } returns mockk()
    }

    fun createInstance() = DccJsonSchemaValidator(
        dccJsonSchema = DccJsonSchema(assetManager),
        schemaValidator = JsonSchemaValidator()
    )

    @Test
    fun `schema pass`() {
        createInstance().apply {
            isValid(JSON_VALID).apply {
                isValid shouldBe true
                invalidityReason shouldBe null
            }
        }
    }

    @Test
    fun `schema fail`() {
        createInstance().apply {
            isValid(JSON_INVALID).apply {
                isValid shouldBe false
                invalidityReason shouldContain "does not match the regex pattern "
            }
        }
    }

    companion object {
        private val JSON_VALID = """
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
        private val JSON_INVALID = """
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
    }
}
