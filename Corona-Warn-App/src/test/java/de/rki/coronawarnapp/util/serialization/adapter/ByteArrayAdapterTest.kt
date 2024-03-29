package de.rki.coronawarnapp.util.serialization.adapter

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ByteArrayAdapterTest : BaseTest() {

    private val mapper = SerializationModule.jacksonBaseMapper

    // This is actually an app config, some cases like did not trigger a few serialization issues in the server test.
    private val goodByteArray = (
        "080b124d0a230a034c4f57180f221a68747470733a2f2f777777" +
            "2e636f726f6e617761726e2e6170700a260a0448494748100f1848221a68747470733a2f2f7777772e636f7" +
            "26f6e617761726e2e6170701a640a10080110021803200428053006380740081100000000000049401a0a20" +
            "0128013001380140012100000000000049402a1008051005180520052805300538054005310000000000003" +
            "4403a0e1001180120012801300138014001410000000000004940221c0a040837103f121209000000000000" +
            "f03f11000000000000e03f20192a1a0a0a0a041008180212021005120c0a0408011804120408011804"
        ).decodeHex().toByteArray()

    @Test
    fun `serialize and deserialize`() {
        val serialized: String = mapper.writeValueAsString(TestData(goodByteArray))

        mapper.readValue<TestData>(serialized) shouldBe TestData(goodByteArray)
    }

    @Test
    fun `malformed base64 should throw specific exception`() {
        shouldThrow<JsonMappingException> {
            """
                {
                    "byteArray": "Don't feed this to your base 64 decoder :("
                }
            """.trimIndent().let { mapper.readValue<TestData>(it) }
        }
    }

    @Test
    fun `empty base64 string is OK`() {
        """
            {
                "byteArray": ""
            }
        """.trimIndent().let {
            mapper.readValue<TestData>(it) shouldBe TestData(ByteArray(0))
        }
    }

    data class TestData(
        val byteArray: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestData

            if (!byteArray.contentEquals(other.byteArray)) return false

            return true
        }

        override fun hashCode(): Int = byteArray.contentHashCode()
    }
}
