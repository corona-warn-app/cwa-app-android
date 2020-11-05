package de.rki.coronawarnapp.diagnosiskeys.download

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class KeySyncToolTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

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

    fun createInstance(): KeyPackageSyncTool = TODO()

    @Test
    fun `normal call sequence`() {
        TODO()
        // Check lastDownload
        // Call sync tools
        // Update lastDownload
        // query keyCache
    }

    @Test
    fun `last download starts failed, and is set successful after sync completes`() {
        TODO()
        // Both Day- HourSyncTool need to return successful sync for the download to be considered successful
    }

    @Test
    fun `failed day sync is reflected in results property`() {
        TODO()
    }

    @Test
    fun `missing last download causes force sync`() {
        TODO()
    }

    @Test
    fun `failed last download causes force sync`() {
        TODO()
    }
}
