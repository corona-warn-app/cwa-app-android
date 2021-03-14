package de.rki.coronawarnapp.bugreporting.debuglog.internal

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runBlockingTest
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
    fun `log size returns current logfile size`() = runBlockingTest {
        logFile.parentFile?.mkdirs()
        logFile.appendBytes(Random.nextBytes(22))
        createInstance().apply {
            setup()
            logSize.value shouldBe 22L
            teardown()
            logSize.value shouldBe 0L
        }
    }
}
