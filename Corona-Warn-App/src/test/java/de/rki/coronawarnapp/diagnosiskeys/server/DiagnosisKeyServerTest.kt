package de.rki.coronawarnapp.diagnosiskeys.server

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseIOTest
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

class DiagnosisKeyServerTest : BaseIOTest() {

    @MockK
    lateinit var api: DiagnosisKeyApiV1

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    private fun createDownloadServer() = DiagnosisKeyServer(
        diagnosisKeyAPI = { api }
    )

    @Test
    fun `download country index`() {
        val downloadServer = createDownloadServer()
        coEvery { api.getLocationIndex() } returns listOf("DE", "NL")

        runTest {
            downloadServer.getLocationIndex() shouldBe listOf(
                LocationCode("DE"),
                LocationCode("NL")
            )
        }

        coVerify { api.getLocationIndex() }
    }

    @Test
    fun `download day index for country`() {
        val downloadServer = createDownloadServer()
        coEvery { api.getDayIndex("DE") } returns listOf(
            "2000-01-01",
            "2000-01-02"
        )

        runTest {
            downloadServer.getDayIndex(LocationCode("DE")) shouldBe listOf(
                "2000-01-01",
                "2000-01-02"
            ).map { LocalDate.parse(it) }
        }

        coVerify { api.getDayIndex("DE") }
    }

    @Test
    fun `download hour index for country and day`() {
        val downloadServer = createDownloadServer()
        coEvery { api.getHourIndex("DE", "2000-01-01") } returns listOf(
            "1",
            "2",
            "20",
            "21"
        )

        runTest {
            downloadServer.getHourIndex(
                LocationCode("DE"),
                LocalDate.parse("2000-01-01")
            ) shouldBe listOf(
                "01:00",
                "02:00",
                "20:00",
                "21:00"
            ).map { LocalTime.parse(it) }
        }

        coVerify { api.getHourIndex("DE", "2000-01-01") }
    }

    @Test
    fun `download key files for day`() {
        val downloadServer = createDownloadServer()
        coEvery {
            api.downloadKeyFileForDay(
                "DE",
                "2000-01-01"
            )
        } returns Response.success("testdata-day".toResponseBody())

        val targetFile = File(testDir, "day-keys")

        runTest {
            downloadServer.downloadKeyFile(
                locationCode = LocationCode("DE"),
                day = LocalDate.parse("2000-01-01"),
                hour = null,
                saveTo = targetFile
            )
        }

        targetFile.exists() shouldBe true
        targetFile.readText() shouldBe "testdata-day"
    }

    @Test
    fun `download key files for hour and check hour format`() {
        val downloadServer = createDownloadServer()

        runTest {
            coEvery {
                api.downloadKeyFileForHour(
                    "DE",
                    "2000-01-01",
                    "1" // no leading ZEROS!
                )
            } returns Response.success("testdata-hour".toResponseBody())

            val targetFile = File(testDir, "hour-keys")

            downloadServer.downloadKeyFile(
                locationCode = LocationCode("DE"),
                day = LocalDate.parse("2000-01-01"),
                hour = LocalTime.parse("01:00"),
                saveTo = targetFile
            )

            targetFile.exists() shouldBe true
            targetFile.readText() shouldBe "testdata-hour"
        }

        runTest {
            coEvery {
                api.downloadKeyFileForHour(
                    "DE",
                    "2000-01-01",
                    "13"
                )
            } returns Response.success("testdata-hour".toResponseBody())

            val targetFile = File(testDir, "hour-keys")

            downloadServer.downloadKeyFile(
                locationCode = LocationCode("DE"),
                day = LocalDate.parse("2000-01-01"),
                hour = LocalTime.parse("13:00"),
                saveTo = targetFile
            )

            targetFile.exists() shouldBe true
            targetFile.readText() shouldBe "testdata-hour"
        }
    }
}
