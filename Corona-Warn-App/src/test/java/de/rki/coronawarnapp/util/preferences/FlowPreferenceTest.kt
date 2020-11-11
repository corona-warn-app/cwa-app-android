package de.rki.coronawarnapp.util.preferences

import com.google.gson.Gson
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJson
import testhelpers.preferences.MockSharedPreferences

class FlowPreferenceTest : BaseTest() {

    private val mockPreferences = MockSharedPreferences()

    @Test
    fun `reading and writing strings`() = runBlockingTest {
        mockPreferences.createFlowPreference<String?>(
            key = "testKey",
            defaultValue = "default"
        ).apply {
            value shouldBe "default"
            flow.first() shouldBe "default"
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe "default"
                "newvalue"
            }

            value shouldBe "newvalue"
            flow.first() shouldBe "newvalue"
            mockPreferences.dataMapPeek.values.first() shouldBe "newvalue"

            update {
                it shouldBe "newvalue"
                null
            }
            value shouldBe "default"
            flow.first() shouldBe "default"
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    @Test
    fun `reading and writing boolean`() = runBlockingTest {
        mockPreferences.createFlowPreference<Boolean?>(
            key = "testKey",
            defaultValue = true
        ).apply {
            value shouldBe true
            flow.first() shouldBe true
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe true
                false
            }

            value shouldBe false
            flow.first() shouldBe false
            mockPreferences.dataMapPeek.values.first() shouldBe false

            update {
                it shouldBe false
                null
            }
            value shouldBe true
            flow.first() shouldBe true
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    @Test
    fun `reading and writing long`() = runBlockingTest {
        mockPreferences.createFlowPreference<Long?>(
            key = "testKey",
            defaultValue = 9000L
        ).apply {
            value shouldBe 9000L
            flow.first() shouldBe 9000L
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe 9000L
                9001L
            }

            value shouldBe 9001L
            flow.first() shouldBe 9001L
            mockPreferences.dataMapPeek.values.first() shouldBe 9001L

            update {
                it shouldBe 9001L
                null
            }
            value shouldBe 9000L
            flow.first() shouldBe 9000L
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    @Test
    fun `reading and writing integer`() = runBlockingTest {
        mockPreferences.createFlowPreference<Long?>(
            key = "testKey",
            defaultValue = 123
        ).apply {
            value shouldBe 123
            flow.first() shouldBe 123
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe 123
                44
            }

            value shouldBe 44
            flow.first() shouldBe 44
            mockPreferences.dataMapPeek.values.first() shouldBe 44

            update {
                it shouldBe 44
                null
            }
            value shouldBe 123
            flow.first() shouldBe 123
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    @Test
    fun `reading and writing float`() = runBlockingTest {
        mockPreferences.createFlowPreference<Float?>(
            key = "testKey",
            defaultValue = 3.6f
        ).apply {
            value shouldBe 3.6f
            flow.first() shouldBe 3.6f
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe 3.6f
                15000f
            }

            value shouldBe 15000f
            flow.first() shouldBe 15000f
            mockPreferences.dataMapPeek.values.first() shouldBe 15000f

            update {
                it shouldBe 15000f
                null
            }
            value shouldBe 3.6f
            flow.first() shouldBe 3.6f
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    data class TestGson(
        val string: String = "",
        val boolean: Boolean = true,
        val float: Float = 1.0f,
        val int: Int = 1,
        val long: Long = 1L
    )

    @Test
    fun `reading and writing GSON`() = runBlockingTest {
        val testData1 = TestGson(string = "teststring")
        val testData2 = TestGson(string = "update")
        FlowPreference<TestGson?>(
            preferences = mockPreferences,
            key = "testKey",
            reader = FlowPreference.gsonReader(Gson(), testData1),
            writer = FlowPreference.gsonWriter(Gson())
        ).apply {
            value shouldBe testData1
            flow.first() shouldBe testData1
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe testData1
                it!!.copy(string = "update")
            }

            value shouldBe testData2
            flow.first() shouldBe testData2
            (mockPreferences.dataMapPeek.values.first() as String).toComparableJson() shouldBe """
                {
                    "string":"update",
                    "boolean":true,
                    "float":1.0,
                    "int":1,
                    "long":1
                }
            """.toComparableJson()

            update {
                it shouldBe testData2
                null
            }
            value shouldBe testData1
            flow.first() shouldBe testData1
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }
}
