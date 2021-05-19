package de.rki.coronawarnapp.bugreporting.debuglog

import android.app.Application
import dagger.Lazy
import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCensor
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLogTree
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.coroutines.test
import testhelpers.logging.JUnitTree
import timber.log.Timber
import java.io.File

@Suppress("BlockingMethodInNonBlockingContext")
class DebugLoggerTest : BaseIOTest() {

    @MockK lateinit var application: Application
    @MockK lateinit var component: ApplicationComponent
    @MockK lateinit var coronaTestCensor: CoronaTestCensor

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val cacheDir = File(testDir, "cache")
    private val debugLogDir = File(cacheDir, "debuglog")
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
            logger.bugCensors = Lazy { setOf(coronaTestCensor) }
        }

        coEvery { coronaTestCensor.checkLog(any()) } returns null
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
        Timber.uprootAll()
    }

    private fun createInstance(scope: CoroutineScope) = DebugLogger(
        context = application,
        scope = scope
    )

    @Test
    fun `init does nothing if there is no trigger file`() = runBlockingTest {
        createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe false
        }
        runningLog.exists() shouldBe false
        Timber.forest().apply {
            size shouldBe 1
            (first() is JUnitTree) shouldBe true
        }
    }

    @Test
    fun `init calls start if there is a trigger file`() = runBlockingTest {
        triggerFile.parentFile?.mkdirs()
        triggerFile.createNewFile()

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe true
        }

        runningLog.exists() shouldBe true

        instance.stop()
    }

    @Test
    fun `init calls start if it is a tester build`() = runBlockingTest {
        every { CWADebug.isDeviceForTestersBuild } returns true

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe true
        }

        runningLog.exists() shouldBe true

        instance.stop()
    }

    @Test
    fun `start plants a tree and starts a logging coroutine`() = runBlockingTest {
        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe false
        }

        Timber.forest().none { it is DebugLogTree } shouldBe true

        instance.start()
        Timber.forest().single { it is DebugLogTree } shouldNotBe null

        instance.stop()
        Timber.forest().none { it is DebugLogTree } shouldBe true
    }

    @Test
    fun `multiple start have no effect`() = runBlockingTest {
        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe false
        }

        Timber.forest().none { it is DebugLogTree } shouldBe true

        instance.start()
        instance.start()
        instance.start()

        Timber.forest().single { it is DebugLogTree } shouldNotBe null

        instance.stop()
        instance.stop()

        Timber.forest().none { it is DebugLogTree } shouldBe true
        instance.isLogging.value shouldBe false
    }

    @Test
    fun `stop cancels the coroutine and uproots the tree and deletes any logs`() = runBlockingTest {
        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe false
        }

        Timber.forest().none { it is DebugLogTree } shouldBe true

        instance.start()
        Timber.forest().single { it is DebugLogTree } shouldNotBe null

        instance.stop()
        Timber.forest().none { it is DebugLogTree } shouldBe true
        instance.isLogging.value shouldBe false

        runningLog.exists() shouldBe false
    }

    @Test
    fun `logwriter is setup and used`() = runBlockingTest {
        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        runBlockingTest {
            instance.start()

            Timber.tag("Tag123").v("Message456")
            advanceTimeBy(2000L)

            runningLog.readLines().last().substring(25) shouldBe """
                V/Tag123: Message456
            """.trimIndent()

            instance.stop()
            advanceUntilIdle()
        }
    }

    @Test
    fun `low storage state is forwarded`() = runBlockingTest {
        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        val testCollector = instance.logState.test(startOnScope = this)

        advanceUntilIdle()
        testCollector.latestValue shouldBe LogState(
            isLogging = false,
            isLowStorage = false,
            logSize = 0L
        )

        instance.start()
        advanceUntilIdle()
        testCollector.latestValue shouldBe LogState(
            isLogging = true,
            isLowStorage = false,
            logSize = 77L
        )

        instance.stop()
        advanceUntilIdle()

        testCollector.latestValue shouldBe LogState(
            isLogging = false,
            isLowStorage = false,
            logSize = 0L
        )

        testCollector.cancel()
    }
}
