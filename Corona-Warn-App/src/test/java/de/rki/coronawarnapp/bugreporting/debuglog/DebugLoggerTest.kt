package de.rki.coronawarnapp.bugreporting.debuglog

import android.app.Application
import dagger.Lazy
import de.rki.coronawarnapp.bugreporting.censors.RegistrationTokenCensor
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.logging.JUnitTree
import timber.log.Timber
import java.io.File
import kotlin.random.Random

class DebugLoggerTest : BaseIOTest() {

    @MockK lateinit var application: Application
    @MockK lateinit var component: ApplicationComponent
    @MockK lateinit var registrationTokenCensor: RegistrationTokenCensor

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val cacheDir = File(testDir, "cache")
    private val debugLogDir = File(cacheDir, "debuglog")
    private val sharedDir = File(debugLogDir, "shared")
    private val runningLog = File(debugLogDir, "debug.log")
    private val triggerFile = File(debugLogDir, "debug.trigger")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(CWADebug)
        every { CWADebug.isDeviceForTestersBuild } returns false

        testDir.mkdirs()
        testDir.exists() shouldBe true

        every { application.cacheDir } returns cacheDir
        every { component.inject(any<DebugLogger>()) } answers {
            val logger = arg<DebugLogger>(0)
            logger.bugCensors = Lazy { listOf(registrationTokenCensor) }
        }
    }

    @AfterEach
    fun teardown() {
        runBlocking { DebugLogger.stop() }
        testDir.deleteRecursively()
        clearAllMocks()
        Timber.uprootAll()
    }

    private fun createInstance() = DebugLogger

    @Test
    fun `init does nothing if there is no trigger file`() {
        createInstance().apply {
            init(application)
            isLogging shouldBe false
        }
        runningLog.exists() shouldBe false
        Timber.forest().apply {
            size shouldBe 1
            (first() is JUnitTree) shouldBe true
        }
    }

    @Test
    fun `init calls start if there is a trigger file`() {
        triggerFile.parentFile?.mkdirs()
        triggerFile.createNewFile()
        createInstance().apply {
            init(application)
            isLogging shouldBe true
        }
        runningLog.exists() shouldBe true
    }

    @Test
    fun `init calls start if it is a tester build`() {
        every { CWADebug.isDeviceForTestersBuild } returns true
        createInstance().apply {
            init(application)
            isLogging shouldBe true
        }
        runningLog.exists() shouldBe true
    }

    @Test
    fun `start plants a tree and starts a logging coroutine`() {
        val instance = createInstance().apply {
            init(application)
            isLogging shouldBe false
        }

        Timber.forest().none { it is DebugLogTree } shouldBe true

        runBlocking {
            instance.start()
            Timber.forest().single { it is DebugLogTree } shouldNotBe null
        }
    }

    @Test
    fun `multiple start have no effect`() {
        val instance = createInstance().apply {
            init(application)
            isLogging shouldBe false
        }

        Timber.forest().none { it is DebugLogTree } shouldBe true

        File(sharedDir, "1").apply {
            parentFile?.mkdirs()
            appendBytes(Random.nextBytes(10))
        }

        runBlocking {
            instance.start()
            instance.start()
            instance.start()

            Timber.forest().single { it is DebugLogTree } shouldNotBe null
            sharedDir.listFiles()!!.size shouldBe 1

            instance.stop()
            instance.stop()

            Timber.forest().none { it is DebugLogTree } shouldBe true
            DebugLogger.isLogging shouldBe false
            sharedDir.listFiles()!!.size shouldBe 0
        }
    }

    @Test
    fun `stop cancels the coroutine and uproots the tree and deletes any logs`() {
        val instance = createInstance().apply {
            init(application)
            isLogging shouldBe false
        }

        Timber.forest().none { it is DebugLogTree } shouldBe true

        runBlocking {
            instance.start()
            Timber.forest().single { it is DebugLogTree } shouldNotBe null

            instance.stop()
            Timber.forest().none { it is DebugLogTree } shouldBe true
            DebugLogger.isLogging shouldBe false

            runningLog.exists() shouldBe false
        }
    }

    @Test
    fun `log size returns current logfile size`() {
        runningLog.parentFile?.mkdirs()
        runningLog.appendBytes(Random.nextBytes(22))
        createInstance().apply {
            init(application)
            getLogSize() shouldBe 22
        }
    }

    @Test
    fun `shared size aggregates shared folder size`() {
        sharedDir.mkdirs()
        File(sharedDir, "1").apply { appendBytes(Random.nextBytes(10)) }
        File(sharedDir, "2").apply { appendBytes(Random.nextBytes(15)) }
        createInstance().apply {
            init(application)
            getShareSize() shouldBe 25
        }
    }
}
