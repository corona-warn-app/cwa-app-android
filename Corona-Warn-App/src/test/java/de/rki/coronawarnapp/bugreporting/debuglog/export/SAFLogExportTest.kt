package de.rki.coronawarnapp.bugreporting.debuglog.export

import android.content.ContentResolver
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import timber.log.Timber
import java.io.File

class SAFLogExportTest : BaseIOTest() {

    @MockK lateinit var contentResolver: ContentResolver

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val zipFile = File(testDir, "logfile.zip")
    private val uriFakeFile = File(testDir, "urifakefile")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        testDir.mkdirs()
        testDir.exists() shouldBe true

        every { contentResolver.openOutputStream(any()) } answers {
            uriFakeFile.outputStream()
        }
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
        Timber.uprootAll()
    }

    private fun createInstance() = SAFLogExport()

    @Test
    fun `request creation and write`() {
        val instance = createInstance()

        zipFile.createNewFile()
        zipFile.exists() shouldBe true
        zipFile.writeText("testcontent")

        val snapshot = LogSnapshotter.Snapshot(zipFile)
        val request = instance.createSAFRequest(snapshot)
        request.snapshot shouldBe snapshot

        request.storeSnapshot(contentResolver, mockk())
        zipFile.exists() shouldBe false
        uriFakeFile.readText() shouldBe "testcontent"
    }

    @Test
    fun `new requests increase id`() {
        val instance = createInstance()
        instance.createSAFRequest(mockk()).id shouldBe 2
        instance.getRequest(2) shouldNotBe null
        instance.createSAFRequest(mockk()).id shouldBe 3
        instance.getRequest(3) shouldNotBe null
        instance.getRequest(4) shouldBe null
    }
}
