package de.rki.coronawarnapp.storage

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import android.os.storage.StorageManager
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.storage.StatsFsProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.io.IOException
import java.util.UUID

class DeviceStorageTest : BaseIOTest() {

    @MockK
    lateinit var context: Context

    private val defaultApiLevel = Build.VERSION_CODES.O
    private val legacyApiLevel = Build.VERSION_CODES.M

    @MockK
    lateinit var storageManager: StorageManager

    @MockK
    lateinit var storageStatsManager: StorageStatsManager

    @MockK
    lateinit var statsFsProvider: StatsFsProvider

    private val privateDataDir = File(IO_TEST_BASEDIR, "privData")
    private val privateDataDirUUID = UUID.randomUUID()

    private val defaultAllocatableBytes = 512L
    private val defaultFreeSpace = 1000 * 1024L
    private val defaultTotalSpace = Long.MAX_VALUE

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(BuildVersionWrap)
        every { BuildVersionWrap.SDK_INT } returns defaultApiLevel

        every { context.filesDir } returns privateDataDir
        every { context.getSystemService(Context.STORAGE_SERVICE) } returns storageManager
        every { context.getSystemService(Context.STORAGE_STATS_SERVICE) } returns storageStatsManager

        every { storageManager.getUuidForPath(privateDataDir) } returns privateDataDirUUID
        every { storageManager.getAllocatableBytes(privateDataDirUUID) } returns defaultAllocatableBytes
        every { storageManager.allocateBytes(any<UUID>(), any()) } returns Unit

        every { storageStatsManager.getFreeBytes(any()) } returns defaultFreeSpace
        every { storageStatsManager.getTotalBytes(any()) } returns defaultTotalSpace

        every { statsFsProvider.createStats(privateDataDir) } returns mockk<StatFs>().apply {
            every { availableBytes } returns defaultFreeSpace
            every { totalBytes } returns defaultTotalSpace
        }
    }

    @AfterEach
    fun teardown() {
        privateDataDir.deleteRecursively()
    }

    private fun buildInstance(): DeviceStorage = DeviceStorage(
        context = context,
        statsFsProvider = statsFsProvider
    )

    @Test
    fun `check private storage space`() {
        val deviceStorage = buildInstance()
        runTest {
            deviceStorage.checkSpacePrivateStorage() shouldBe DeviceStorage.CheckResult(
                path = privateDataDir,
                isSpaceAvailable = true,
                freeBytes = defaultFreeSpace,
                totalBytes = defaultTotalSpace
            )
        }
        verify { storageManager.getUuidForPath(any()) }
        verify(exactly = 0) { statsFsProvider.createStats(any()) }

        verify(exactly = 0) { storageManager.allocateBytes(any<UUID>(), any()) }
    }

    @Test
    fun `check private storage space, sub API26`() {
        every { BuildVersionWrap.SDK_INT } returns legacyApiLevel
        val deviceStorage = buildInstance()
        runTest {
            deviceStorage.checkSpacePrivateStorage() shouldBe DeviceStorage.CheckResult(
                path = privateDataDir,
                isSpaceAvailable = true,
                freeBytes = defaultFreeSpace,
                totalBytes = defaultTotalSpace
            )
        }
        verify(exactly = 0) { storageManager.getUuidForPath(any()) }
        verify { statsFsProvider.createStats(any()) }
    }

    @Test
    fun `request space from private storage successfully`() {
        val deviceStorage = buildInstance()
        runTest {
            deviceStorage.checkSpacePrivateStorage(requiredBytes = defaultFreeSpace) shouldBe DeviceStorage.CheckResult(
                path = privateDataDir,
                isSpaceAvailable = true,
                requiredBytes = defaultFreeSpace,
                freeBytes = defaultFreeSpace,
                totalBytes = defaultTotalSpace
            )
        }
        verify(exactly = 0) { storageManager.allocateBytes(any<UUID>(), any()) }
    }

    @Test
    fun `request space from private storage successfully, sub API26`() {
        every { BuildVersionWrap.SDK_INT } returns legacyApiLevel
        val deviceStorage = buildInstance()
        runTest {
            deviceStorage.checkSpacePrivateStorage(requiredBytes = defaultFreeSpace) shouldBe DeviceStorage.CheckResult(
                path = privateDataDir,
                isSpaceAvailable = true,
                requiredBytes = defaultFreeSpace,
                freeBytes = defaultFreeSpace,
                totalBytes = defaultTotalSpace
            )
        }
    }

    @Test
    fun `request space from private storage wth allocation`() {
        val deviceStorage = buildInstance()
        runTest {
            val targetBytes = defaultFreeSpace + defaultAllocatableBytes
            deviceStorage.checkSpacePrivateStorage(requiredBytes = targetBytes) shouldBe DeviceStorage.CheckResult(
                path = privateDataDir,
                isSpaceAvailable = true,
                requiredBytes = targetBytes,
                freeBytes = targetBytes,
                totalBytes = defaultTotalSpace
            )
        }

        verify { storageManager.allocateBytes(privateDataDirUUID, defaultAllocatableBytes) }
    }

    @Test
    fun `request space from private storage unsuccessfully`() {
        val deviceStorage = buildInstance()
        runTest {
            deviceStorage.checkSpacePrivateStorage(requiredBytes = Long.MAX_VALUE) shouldBe DeviceStorage.CheckResult(
                path = privateDataDir,
                isSpaceAvailable = false,
                freeBytes = defaultFreeSpace,
                requiredBytes = Long.MAX_VALUE,
                totalBytes = defaultTotalSpace
            )
            shouldThrow<InsufficientStorageException> {
                deviceStorage.requireSpacePrivateStorage(Long.MAX_VALUE)
            }
        }
    }

    @Test
    fun `request space from private storage unsuccessfully, sub API26`() {
        every { BuildVersionWrap.SDK_INT } returns legacyApiLevel

        val deviceStorage = buildInstance()
        runTest {
            deviceStorage.checkSpacePrivateStorage(requiredBytes = Long.MAX_VALUE) shouldBe DeviceStorage.CheckResult(
                path = privateDataDir,
                isSpaceAvailable = false,
                requiredBytes = Long.MAX_VALUE,
                freeBytes = defaultFreeSpace,
                totalBytes = defaultTotalSpace
            )
        }
    }

    @Test
    fun `check private storage space, fallback in error case`() {
        every { storageManager.getUuidForPath(privateDataDir) } throws IOException("uh oh")

        val deviceStorage = buildInstance()
        runTest {
            deviceStorage.checkSpacePrivateStorage() shouldBe DeviceStorage.CheckResult(
                path = privateDataDir,
                isSpaceAvailable = true,
                freeBytes = defaultFreeSpace,
                totalBytes = defaultTotalSpace
            )
        }

        verify { statsFsProvider.createStats(privateDataDir) }
    }

    @Test
    fun `check private storage space, sub API26, error case has no fallback`() {
        every { BuildVersionWrap.SDK_INT } returns legacyApiLevel
        every { statsFsProvider.createStats(privateDataDir) } throws IOException("uh oh")

        val deviceStorage = buildInstance()
        runTest {
            shouldThrow<IOException> { deviceStorage.checkSpacePrivateStorage() }
        }
    }
}
