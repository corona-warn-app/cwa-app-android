package de.rki.coronawarnapp.util.serialization

import com.fasterxml.jackson.core.io.JsonEOFException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.util.UUID

class JacksonExtensionsTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val testFile = File(testDir, "testfile")
    private val mapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        testDir.mkdirs()
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    data class TestData(
        val value: String
    )

    @Test
    fun `serialize and deserialize`() {
        val testData = TestData(value = UUID.randomUUID().toString())
        mapper.writeValue(testData, testFile)

        mapper.readValue<TestData>(testFile) shouldBe testData
    }

    @Test
    fun `deserialize an empty file`() {
        testFile.createNewFile()
        testFile.exists() shouldBe true

        val testData: TestData? = mapper.readValue(testFile)

        testData shouldBe null

        testFile.exists() shouldBe false
    }

    @Test
    fun `deserialize a malformed file`() {
        testFile.writeText("{")

        shouldThrow<JsonEOFException> {
            mapper.readValue(testFile)
        }
    }
}
