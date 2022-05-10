package de.rki.coronawarnapp.bugreporting.debuglog.internal

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import timber.log.Timber
import java.io.File
import kotlin.random.Random

class LogWriterTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val logFile = File(testDir, "logfile.log")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
        Timber.uprootAll()
    }

    private fun createInstance() = LogWriter(
        logFile = logFile
    )

    @Test
    fun `log size returns current logfile size`() = runTest {
        logFile.parentFile?.mkdirs()
        logFile.appendBytes(Random.nextBytes(22))
        createInstance().apply {
            setup()
            logSize.value shouldBe 22L
            teardown()
            logSize.value shouldBe 0L
        }
    }

    /**
     * e.g. System cache cleaning interferring
     */
    @Test
    fun `if the file is deleted after setup we try to recreate it and do not crash`() = runTest {
        createInstance().apply {
            setup()
            write("ABC")
            logFile.readText() shouldBe "ABC\n"

            logSize.value shouldBe 4L

            logFile.delete()
            logFile.parentFile!!.delete()
            logFile.exists() shouldBe false

            write("DEF")
            logFile.readText() shouldBe "Logfile was deleted.\nDEF\n"

            logSize.value shouldBe 25L
        }
    }
}
