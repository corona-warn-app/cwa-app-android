package de.rki.coronawarnapp.diagnosiskeys.server

import dagger.Lazy
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseIOTest
import java.io.File

class DiagnosisKeyServerTest : BaseIOTest() {

    @MockK
    lateinit var api: DiagnosisKeyApiV1

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private val defaultHomeCountry = LocationCode("DE")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createDownloadServer(
        homeCountry: LocationCode = defaultHomeCountry
    ) = DiagnosisKeyServer(
        diagnosisKeyAPI = Lazy { api },
        homeCountry = homeCountry
    )

    @Test
    fun `download country index`() {
        val downloadServer = createDownloadServer()
        coEvery { api.getCountryIndex() } returns listOf("DE", "NL")

        runBlocking {
            downloadServer.getCountryIndex() shouldBe listOf(
                LocationCode("DE"), LocationCode("NL")
            )
        }

        coVerify { api.getCountryIndex() }
    }

    @Test
    fun `download day index for country`() {
        val downloadServer = createDownloadServer()
        coEvery { api.getDayIndex("DE") } returns listOf(
            "2000-01-01", "2000-01-02"
        )

        runBlocking {
            downloadServer.getDayIndex(LocationCode("DE")) shouldBe listOf(
                "2000-01-01", "2000-01-02"
            ).map { LocalDate.parse(it) }
        }

        coVerify { api.getDayIndex("DE") }
    }

    @Test
    fun `download hour index for country and day`() {
        val downloadServer = createDownloadServer()
        coEvery { api.getHourIndex("DE", "2000-01-01") } returns listOf(
            "1", "2", "20", "21"
        )

        runBlocking {
            downloadServer.getHourIndex(
                LocationCode("DE"),
                LocalDate.parse("2000-01-01")
            ) shouldBe listOf(
                "01:00", "02:00", "20:00", "21:00"
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

        runBlocking {
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

        runBlocking {
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

        runBlocking {
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
