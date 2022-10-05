package de.rki.coronawarnapp.bugreporting.debuglog.internal

import android.content.Context
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.Instant
import java.time.format.DateTimeFormatter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import timber.log.Timber
import java.io.File

class LogSnapshotterTest : BaseIOTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var debugLogger: DebugLogger
    @MockK lateinit var timeStamper: TimeStamper

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val cacheDir = File(testDir, "cache")
    private val runningLogFake = File(testDir, "running.log")

    private val snapshotDir = File(cacheDir, "debuglog_snapshots")
    private val fileNameDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss.SSS")
    private val userTime = Instant.EPOCH.toLocalDateTimeUserTz()
    private val expectedSnapshot = File(snapshotDir, "CWA Log ${userTime.format(fileNameDateFormatter)}.zip")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.cacheDir } returns cacheDir

        testDir.mkdirs()
        testDir.exists() shouldBe true

        every { debugLogger.runningLog } returns runningLogFake
        every { timeStamper.nowUTC } returns Instant.EPOCH

        runningLogFake.parentFile!!.mkdirs()
        runningLogFake.writeText("1 Doge = 1 Doge")
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
        Timber.uprootAll()
    }

    private fun createInstance() = LogSnapshotter(
        context = context,
        debugLogger = debugLogger,
        timeStamper = timeStamper
    )

    @Test
    fun `normal snapshot`() {
        val instance = createInstance()

        val snapshot = instance.snapshot()

        snapshot.apply {
            path shouldBe expectedSnapshot
            path.exists() shouldBe true
            path.length() shouldBe 197L
        }

        snapshot.apply {
            delete()
            snapshot.path.exists() shouldBe false
        }
    }
}
