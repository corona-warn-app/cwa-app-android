package de.rki.coronawarnapp.util.serialization

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.util.UUID

class GsonExtensionsTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val testFile = File(testDir, "testfile")
    private val gson = Gson()

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
        gson.toJson(testData, testFile)

        gson.fromJson<TestData>(testFile) shouldBe testData
    }

    @Test
    fun `deserialize an empty file`() {
        testFile.createNewFile()
        testFile.exists() shouldBe true

        val testData: TestData? = gson.fromJson(testFile)

        testData shouldBe null

        testFile.exists() shouldBe false
    }

    @Test
    fun `deserialize a malformed file`() {
        testFile.writeText("{")

        shouldThrow<JsonSyntaxException> {
            gson.fromJson(testFile)
        }
    }
}
