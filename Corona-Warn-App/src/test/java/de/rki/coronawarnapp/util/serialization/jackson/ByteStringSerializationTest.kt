package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.HashExtensions.sha256
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test

class ByteStringSerializationTest {

    private val objectMapper = SerializationModule().jacksonObjectMapper()

    private val emptyByteString = ByteString.EMPTY
    private val goodByteString = "Good ByteString".sha256()
    private val optionalByteString = "Optional ByteString".toByteArray().toByteString()

    @Test
    fun `serialize and deserialize`() {
        val emptyTestData = TestData(emptyByteString)
        val emptySerialized: String = objectMapper.writeValueAsString(emptyTestData)
        objectMapper.readValue<TestData>(emptySerialized) shouldBe emptyTestData

        val goodTestData = TestData(goodByteString)
        val goodSerialized: String = objectMapper.writeValueAsString(goodTestData)
        objectMapper.readValue<TestData>(goodSerialized) shouldBe goodTestData

        val goodWithOptionalTestData = TestData(goodByteString, optionalByteString)
        val goodWithOptionalSerialized: String = objectMapper.writeValueAsString(goodWithOptionalTestData)
        objectMapper.readValue<TestData>(goodWithOptionalSerialized) shouldBe goodWithOptionalTestData
    }

    @Test
    fun `malformed base64 should throw specific exception`() {
        shouldThrow<JsonMappingException> {
            """
                {
                    "byteString": "Don't feed this to your base 64 decoder :("
                }
            """.trimIndent().let { objectMapper.readValue<TestData>(it) }
        }
    }

    @Test
    fun `empty base64 string is OK`() {
        """
            {
                "byteString": ""
            }
        """.trimIndent().let {
            objectMapper.readValue<TestData>(it) shouldBe TestData(ByteString.EMPTY)
        }
    }

    data class TestData(
        val byteString: ByteString,
        val optional: ByteString? = null
    )
}
