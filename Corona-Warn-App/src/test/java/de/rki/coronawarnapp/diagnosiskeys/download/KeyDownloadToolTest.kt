package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadInfo
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import java.time.Duration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class KeyDownloadToolTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val testFile = File(testDir, "testfile")

    @MockK private lateinit var keyServer: DiagnosisKeyServer
    @MockK private lateinit var keyCache: KeyCacheRepository
    @MockK private lateinit var downloadConfig: KeyDownloadConfig
    @MockK private lateinit var cachedKey: CachedKey

    private val cachedKeyInfo = CachedKeyInfo(
        type = CachedKeyInfo.Type.LOCATION_DAY,
        location = LocationCode("EUR"),
        day = LocalDate.parse("2000-01-01"),
        hour = LocalTime.parse("20:00"),
        createdAt = Instant.EPOCH
    )
    private val downloadInfo = DownloadInfo(
        headers = Headers.headersOf("ETag", "I'm an ETag :).")
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        coEvery { keyServer.downloadKeyFile(any(), any(), any(), any(), any()) } returns downloadInfo

        every { cachedKey.path } returns testFile
        every { cachedKey.info } returns cachedKeyInfo

        every { downloadConfig.individualDownloadTimeout } returns Duration.ofMillis(9000L)

        coEvery { keyCache.markKeyComplete(any(), any()) } just Runs
        coEvery { keyCache.deleteInfoAndFile(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    private fun createInstance() = KeyDownloadTool(
        keyServer = keyServer,
        keyCache = keyCache
    )

    @Test
    fun `etag from header is stored`() = runTest {
        val instance = createInstance()

        instance.downloadKeyFile(cachedKey, downloadConfig)

        coVerify { keyCache.markKeyComplete(cachedKeyInfo, "I'm an ETag :).") }
    }

    @Test
    fun `if the etag is missing we throw an exception`() = runTest {
        coEvery { keyServer.downloadKeyFile(any(), any(), any(), any(), any()) } returns DownloadInfo(
            headers = Headers.headersOf()
        )

        testFile.writeText("Good Morning")

        val instance = createInstance()

        shouldThrow<IllegalArgumentException> {
            instance.downloadKeyFile(cachedKey, downloadConfig)
        }
    }

    @Test
    fun `invididual downloads timeout based on appconfig`() = runTest {
        coEvery { keyServer.downloadKeyFile(any(), any(), any(), any(), any()) } coAnswers {
            delay(10 * 1000)
            mockk()
        }

        val instance = createInstance()

        advanceUntilIdle()

        shouldThrow<TimeoutCancellationException> {
            instance.downloadKeyFile(cachedKey, downloadConfig)
        }
    }

    @Test
    fun `failed downloads are deleted`() = runTest {
        coEvery { keyServer.downloadKeyFile(any(), any(), any(), any(), any()) } throws IOException()

        val instance = createInstance()

        advanceUntilIdle()

        shouldThrow<IOException> {
            instance.downloadKeyFile(cachedKey, downloadConfig)
        }

        coVerify { keyCache.deleteInfoAndFile(listOf(cachedKeyInfo)) }
    }
}
