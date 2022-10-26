package de.rki.coronawarnapp.nearby.modules.detectiontracker

import android.content.Context
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.extensions.toComparableJsonPretty
import java.io.File
import java.nio.file.Paths
import java.time.Instant

class ExposureDetectionTrackerStorageTest : BaseIOTest() {

    @MockK private lateinit var context: Context

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val privateFiles = File(testDir, "files")
    private val storageDir = File(privateFiles, "calcuation_tracker")
    private val storageFile = File(storageDir, "calculations.json")

    private val demoJsonString =
        """
            {
              "b2b98400-058d-43e6-b952-529a5255248b": {
                "identifier": "b2b98400-058d-43e6-b952-529a5255248b",
                "startedAt": {
                  "iMillis": 1603473968125
                },
                "enfVersion": "V2_WINDOW_MODE"
              },
              "aeb15509-fb34-42ce-8795-7a9ae0c2f389": {
                "identifier": "aeb15509-fb34-42ce-8795-7a9ae0c2f389",
                "startedAt": {
                  "iMillis": 1603473968125
                },
                "result": "UPDATED_STATE",
                "finishedAt": {
                  "iMillis": 1603473968125
                },
                "enfVersion": "V1_LEGACY_MODE"
              }
            }
        """.trimIndent()

    private val demoJsonStringAfterMigration = """
            {
              "b2b98400-058d-43e6-b952-529a5255248b": {
                "identifier": "b2b98400-058d-43e6-b952-529a5255248b",
                "startedAt": 1603473968125,
                "enfVersion": "V2_WINDOW_MODE"
              },
              "aeb15509-fb34-42ce-8795-7a9ae0c2f389": {
                "identifier": "aeb15509-fb34-42ce-8795-7a9ae0c2f389",
                "startedAt": 1603473968125,
                "result": "UPDATED_STATE",
                "finishedAt": 1603473968125,
                "enfVersion": "V1_LEGACY_MODE"
              }
            }
    """.trimIndent()

    private val demoData = run {
        val calculation1 = TrackedExposureDetection(
            identifier = "b2b98400-058d-43e6-b952-529a5255248b",
            startedAt = Instant.ofEpochMilli(1603473968125),
            enfVersion = TrackedExposureDetection.EnfVersion.V2_WINDOW_MODE
        )
        val calculation2 = TrackedExposureDetection(
            identifier = "aeb15509-fb34-42ce-8795-7a9ae0c2f389",
            startedAt = Instant.ofEpochMilli(1603473968125),
            finishedAt = Instant.ofEpochMilli(1603473968125),
            result = TrackedExposureDetection.Result.UPDATED_STATE,
            enfVersion = TrackedExposureDetection.EnfVersion.V1_LEGACY_MODE
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
        testDir.deleteRecursively()
    }

    private fun createStorage() = ExposureDetectionTrackerStorage(
        context = context,
        baseGson = SerializationModule.baseGson
    )

    @Test
    fun `init is side effect free`() = runTest {
        createStorage()
        storageDir.exists() shouldBe false
        createStorage().save(emptyMap())
        storageDir.exists() shouldBe true
    }

    @Test
    fun `load empty on non-existing file`() = runTest {
        createStorage().load() shouldBe emptyMap()
    }

    @Test
    fun `save on unchanged data does nothing`() = runTest {
        storageDir.mkdirs()
        storageFile.writeText(demoJsonStringAfterMigration)

        createStorage().apply {
            load() shouldBe demoData
            storageFile.delete()
            save(demoData)
            storageFile.exists() shouldBe false
        }
    }

    @Test
    fun `save changed data after migration`() = runTest {
        storageDir.mkdirs()
        storageFile.writeText(demoJsonString)

        createStorage().apply {
            load() shouldBe demoData

            storageFile.delete()

            save(demoData)
            storageFile.exists() shouldBe true
            storageFile.readText().toComparableJsonPretty() shouldBe demoJsonStringAfterMigration
        }
    }

    @Test
    fun `saving data creates a json file`() = runTest {
        createStorage().save(demoData)
        storageFile.exists() shouldBe true
        val json = SerializationModule.baseGson.toJson(demoData)
        json shouldBe storageFile.readText()
        json.toComparableJsonPretty() shouldBe demoJsonStringAfterMigration
    }

    @Test
    fun `gson does weird things to property initialization`() = runTest {
        every { context.filesDir } returns Paths.get("src/test/resources", "detectionLegacy").toFile()
        // This makes sure we are using val-getters, otherwise gson inits our @Ŧransient properties to false
        val storedData: Map<String, TrackedExposureDetection> = createStorage().load()
        storedData.getValue("b2b98400-058d-43e6-b952-529a5255248b").apply {
            isCalculating shouldBe true
            startedAt shouldBe Instant.ofEpochMilli(1603473968125)
            finishedAt shouldBe null
        }
        storedData.getValue("aeb15509-fb34-42ce-8795-7a9ae0c2f389").apply {
            isCalculating shouldBe false
            startedAt shouldBe Instant.ofEpochMilli(1603473968125)
            finishedAt shouldBe Instant.ofEpochMilli(1603473968125)
        }
    }

    @Test
    fun `gson does weird things to property initialization - 2`() = runTest {
        every { context.filesDir } returns Paths.get("src/test/resources", "detectionJavaTime").toFile()
        // This makes sure we are using val-getters, otherwise gson inits our @Ŧransient properties to false
        val storedData: Map<String, TrackedExposureDetection> = createStorage().load()
        storedData.getValue("b2b98400-058d-43e6-b952-529a5255248b").apply {
            isCalculating shouldBe true
            startedAt shouldBe Instant.ofEpochMilli(1603473968125)
            finishedAt shouldBe null
        }

        storedData.getValue("aeb15509-fb34-42ce-8795-7a9ae0c2f389").apply {
            isCalculating shouldBe false
            startedAt shouldBe Instant.ofEpochMilli(1603473968125)
            finishedAt shouldBe Instant.ofEpochMilli(1603473968125)
        }
    }

    @Test
    fun `we catch empty json data and prevent unsafely initialized maps`() = runTest {
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
