package de.rki.coronawarnapp.nearby.modules.calculationtracker

import android.content.Context
import com.google.gson.GsonBuilder
import de.rki.coronawarnapp.util.gson.fromJson
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class CalculationTrackerStorageTest : BaseIOTest() {

    @MockK private lateinit var context: Context

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val privateFiles = File(testDir, "files")
    private val storageDir = File(privateFiles, "calcuation_tracker")
    private val storageFile = File(storageDir, "calculations.json")

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val demoJsonString = """
            {
              "b2b98400-058d-43e6-b952-529a5255248b": {
                "identifier": "b2b98400-058d-43e6-b952-529a5255248b",
                "state": "CALCULATING",
                "startedAt": {
                  "iMillis": 1234
                }
              },
              "aeb15509-fb34-42ce-8795-7a9ae0c2f389": {
                "identifier": "aeb15509-fb34-42ce-8795-7a9ae0c2f389",
                "state": "DONE",
                "startedAt": {
                  "iMillis": 5678
                },
                "result": "UPDATED_STATE",
                "finishedAt": {
                  "iMillis": 1603473968125
                }
              }
            }
        """.trimIndent()

    private val demoData = run {
        val calculation1 = Calculation(
            identifier = "b2b98400-058d-43e6-b952-529a5255248b",
            state = Calculation.State.CALCULATING,
            startedAt = Instant.ofEpochMilli(1234)
        )
        val calculation2 = Calculation(
            identifier = "aeb15509-fb34-42ce-8795-7a9ae0c2f389",
            startedAt = Instant.ofEpochMilli(5678),
            state = Calculation.State.DONE,
            finishedAt = Instant.ofEpochMilli(1603473968125),
            result = Calculation.Result.UPDATED_STATE
        )
        mapOf(
            calculation1.identifier to calculation1,
            calculation2.identifier to calculation2
        )
    }

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.filesDir } returns privateFiles
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createStorage() = CalculationTrackerStorage(context)

    @Test
    fun `init is sideeffect free`() = runBlockingTest {
        createStorage()
        storageDir.exists() shouldBe false
        createStorage().save(emptyMap())
        storageDir.exists() shouldBe true
    }

    @Test
    fun `load empty on non-existing file`() = runBlockingTest {
        createStorage().load() shouldBe emptyMap()
    }

    @Test
    fun `save on unchanged data does nothing`() = runBlockingTest {
        storageDir.mkdirs()
        storageFile.writeText(demoJsonString)

        createStorage().apply {
            load()

            storageFile.delete()

            save(demoData)
            storageFile.exists() shouldBe false
        }
    }

    @Test
    fun `saving data creates a json file`() = runBlockingTest {

        createStorage().save(demoData)
        storageFile.exists() shouldBe true

        val storedData: Map<String, Calculation> = gson.fromJson(storageFile)

        storedData shouldBe demoData
        gson.toJson(storedData) shouldBe demoJsonString
    }
}
