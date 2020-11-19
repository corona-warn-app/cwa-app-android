package de.rki.coronawarnapp.nearby.modules.detectiontracker

import android.content.Context
import com.google.gson.GsonBuilder
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.fromJson
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

class ExposureDetectionTrackerStorageTest : BaseIOTest() {

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
                "startedAt": {
                  "iMillis": 1234
                }
              },
              "aeb15509-fb34-42ce-8795-7a9ae0c2f389": {
                "identifier": "aeb15509-fb34-42ce-8795-7a9ae0c2f389",
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
        val calculation1 = TrackedExposureDetection(
            identifier = "b2b98400-058d-43e6-b952-529a5255248b",
            startedAt = Instant.ofEpochMilli(1234)
        )
        val calculation2 = TrackedExposureDetection(
            identifier = "aeb15509-fb34-42ce-8795-7a9ae0c2f389",
            startedAt = Instant.ofEpochMilli(5678),
            finishedAt = Instant.ofEpochMilli(1603473968125),
            result = TrackedExposureDetection.Result.UPDATED_STATE
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

    private fun createStorage() = ExposureDetectionTrackerStorage(
        context = context,
        gson = SerializationModule().baseGson()
    )

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

        val storedData: Map<String, TrackedExposureDetection> = gson.fromJson(storageFile)

        storedData shouldBe demoData
        gson.toJson(storedData) shouldBe demoJsonString
    }

    @Test
    fun `gson does weird things to property initialization`() {
        // This makes sure we are using val-getters, otherwise gson inits our @Ŧransient properties to false
        val storedData: Map<String, TrackedExposureDetection> = gson.fromJson(demoJsonString)
        storedData.getValue("b2b98400-058d-43e6-b952-529a5255248b").isCalculating shouldBe true
        storedData.getValue("aeb15509-fb34-42ce-8795-7a9ae0c2f389").isCalculating shouldBe false
    }

    @Test
    fun `we catch empty json data and prevent unsafely initialized maps`() = runBlockingTest {
        storageDir.mkdirs()
        storageFile.writeText("")

        storageFile.exists() shouldBe true

        createStorage().apply {
            val value = load()
            value.size shouldBe 0
            value shouldBe emptyMap()

            storageFile.exists() shouldBe false
        }
    }
}
