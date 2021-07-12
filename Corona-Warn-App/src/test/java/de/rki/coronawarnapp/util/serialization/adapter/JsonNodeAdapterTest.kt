package de.rki.coronawarnapp.util.serialization.adapter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.GsonBuilder
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.fromJson
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJson

class JsonNodeAdapterTest : BaseTest() {

    private val mapper = SerializationModule.jacksonBaseMapper
    private val gson = GsonBuilder()
        .registerTypeAdapter(JsonNode::class.java, JsonNodeAdapter(mapper))
        .create()

    private val innerJson = """
            {
                "and": [
                    {
                        "===": [
                          {
                            "var": "payload.t.0.tg"
                          },
                          "840539006"
                        ]
                    }
                ]
            }
        """.trimIndent()
    private val outerJson = """
            {
                "innerJson": $innerJson
            }
        """.trimIndent()

    @Test
    fun `serialize and deserialize`() {
        val original = TestData(
            innerJson = mapper.readTree(innerJson)
        )
        val serialized: String = gson.toJson(original)

        serialized.toComparableJson() shouldBe outerJson.toComparableJson()

        gson.fromJson<TestData>(serialized).apply {
            this shouldBe original
            innerJson shouldBe innerJson
        }
    }

    @Test
    fun `null value`() {
        val original = TestData(
            innerJson = null
        )
        val serialized: String = gson.toJson(original)

        serialized.toComparableJson() shouldBe "{}".toComparableJson()

        gson.fromJson<TestData>(serialized).apply {
            this shouldBe original
            innerJson shouldBe null
        }
    }

    @Test
    fun `empty json`() {
        val original = TestData(
            innerJson = ObjectMapper().readTree("{}")
        )
        val serialized: String = gson.toJson(original)

        serialized.toComparableJson() shouldBe """
            {
                "innerJson": {}
            }
        """.trimIndent().toComparableJson()

        gson.fromJson<TestData>(serialized).apply {
            this shouldBe original
            innerJson.toString() shouldBe "{}"
        }
    }

    data class TestData(
        val innerJson: JsonNode?
    )
}
