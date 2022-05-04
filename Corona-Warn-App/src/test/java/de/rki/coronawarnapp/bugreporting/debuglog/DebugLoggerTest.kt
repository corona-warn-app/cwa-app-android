package de.rki.coronawarnapp.bugreporting.debuglog

import android.app.Application
import android.content.pm.PackageManager
import dagger.Lazy
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLogTree
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.coroutines.test
import testhelpers.logging.JUnitTree
import timber.log.Timber
import java.io.File

@Suppress("BlockingMethodInNonBlockingContext", "MaxLineLength")
class DebugLoggerTest : BaseIOTest() {

    @MockK lateinit var application: Application
    @MockK lateinit var packageManager: PackageManager
    @MockK lateinit var component: ApplicationComponent
    @MockK lateinit var coronaTestCensor1: BugCensor
    @MockK lateinit var coronaTestCensor2: BugCensor

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
        every { application.packageManager } returns packageManager

        every { component.inject(any<DebugLogger>()) } answers {
            val logger = arg<DebugLogger>(0)
            logger.bugCensors = Lazy { setOf(coronaTestCensor1, coronaTestCensor2) }
        }

        coEvery { coronaTestCensor1.checkLog(any()) } returns null
        coEvery { coronaTestCensor2.checkLog(any()) } returns null
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
    fun `init does nothing if there is no trigger file`() = runTest {
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
    fun `init calls start if there is a trigger file`() = runTest {
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
    fun `init calls start if it is a tester build and autologger pkg is installed`() = runTest {
        every { CWADebug.isDeviceForTestersBuild } returns true

        every {
            packageManager.getPackageInfo("de.rki.coronawarnapp.els.autologger", 0)
        } returns mockk()

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe true
        }

        runningLog.exists() shouldBe true

        instance.stop()
    }

    @Test
    fun `init does not call start on tester builds without the autologger pkg`() = runTest {
        every { CWADebug.isDeviceForTestersBuild } returns true

        every { application.packageManager } returns mockk<PackageManager>().apply {
            every { getPackageInfo(any<String>(), any()) } throws PackageManager.NameNotFoundException()
        }

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe false
        }

        runningLog.exists() shouldBe false

        instance.stop()
    }

    @Test
    fun `init does not call start on tester builds with ROM issues`() = runTest {
        every { CWADebug.isDeviceForTestersBuild } returns true

        every { application.packageManager } throws SecurityException()

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe false
        }

        runningLog.exists() shouldBe false

        instance.stop()
    }

    @Test
    fun `package check is not executed in PROD`() = runTest {
        every { CWADebug.isDeviceForTestersBuild } returns false

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
            isLogging.value shouldBe false
        }

        runningLog.exists() shouldBe false

        instance.stop()

        verify { packageManager wasNot Called }
    }

    @Test
    fun `start plants a tree and starts a logging coroutine`() = runTest {
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
    fun `multiple start have no effect`() = runTest {
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
    fun `stop cancels the coroutine and uproots the tree and deletes any logs`() = runTest {
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
    fun `logwriter is setup and used`() = runTest {
        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        instance.start()

        Timber.tag("Tag123").v("Message456")
        advanceTimeBy(2000L)

        runningLog.readLines().last().substring(25) shouldBe "V/Tag123: Message456"

        instance.stop()
        advanceUntilIdle()
    }

    @Test
    fun `low storage state is forwarded`() = runTest {
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

    @Test
    fun `affected text ranges are removed when censoring collisions occur`() = runTest {
        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        val logMsg = "Lukas says: A hot coffee is really nice!"

        coEvery { coronaTestCensor1.checkLog(any()) } answers {
            val msg = arg<String>(0)
            BugCensor.CensorContainer(msg).censor("says: A hot coffee", "says: A hot tea")
        }

        instance.start()

        Timber.tag("Test").v(logMsg)
        advanceTimeBy(2000L)

        delay(5000)

        runningLog.readLines().last().substring(25) shouldBe "V/Test: Lukas says: A hot tea is really nice!"

        coEvery { coronaTestCensor2.checkLog(any()) } answers {
            val msg = arg<String>(0)
            BugCensor.CensorContainer(msg).censor("says:", "sings:")
        }

        Timber.tag("Test").v(logMsg)
        advanceTimeBy(2000L)

        runningLog.readLines().last().substring(25) shouldBe "V/Test: Lukas <censor-collision/> is really nice!"

        instance.stop()
        advanceUntilIdle()
    }

    @Test
    fun `censoring collision handling for multiple values in the same string`() = runTest {
        val before =
            """
            RACoronaTest(
                identifier=qrcode-RAPID_ANTIGEN-9a9a35fa1cf3261be3349fc50a37b58280634bf42487c8e4eca060c48f259eb7,
                registeredAt=2021-05-25T10:18:05.275Z,
                registrationToken=b0d451f9-a4ea-45ea-b634-f503458a64c9,
                isSubmitted=false, isViewed=true, isAdvancedConsentGiven=true, isJournalEntryCreated=false,
                isResultAvailableNotificationSent=true,
                testResultReceivedAt=2021-05-25T10:18:41.616Z,
                lastUpdatedAt=2021-05-25T10:18:41.152Z,
                testResult=RAT_POSITIVE(7),
                testedAt=2021-05-25T10:17:51.000Z,
                firstName=Rüdiger, lastName=Müller,
                dateOfBirth=1994-05-18, isProcessing=true,
                lastError=null
            )
            updated to
            RACoronaTest(
                identifier=qrcode-RAPID_ANTIGEN-9a9a35fa1cf3261be3349fc50a37b58280634bf42487c8e4eca060c48f259eb7,
                registeredAt=2021-05-25T10:18:05.275Z,
                registrationToken=b0d451f9-a4ea-45ea-b634-f503458a64c9,
                isSubmitted=true, isViewed=true, isAdvancedConsentGiven=true, isJournalEntryCreated=false,
                isResultAvailableNotificationSent=true,
                testResultReceivedAt=2021-05-25T10:18:41.616Z,
                lastUpdatedAt=2021-05-25T10:18:41.152Z,
                testResult=RAT_POSITIVE(7),
                testedAt=2021-05-25T10:17:51.000Z,
                firstName=Rüdiger, lastName=Müller,
                dateOfBirth=1994-05-18, isProcessing=false,
                lastError=IOException()
            )
            """.trimIndent()
        val after =
            """
            RACoronaTest(
                identifier=<censor-collision/>,
                dateOfBirth=1994-05-18, isProcessing=false,
                lastError=IOException()
            )
            """.trimIndent()

        coEvery { coronaTestCensor1.checkLog(any()) } answers {
            val msg = arg<String>(0)
            BugCensor.CensorContainer(msg).censor(
                "firstName=Rüdiger, lastName=Müller",
                "firstName=FIRSTNAME, lastName=LASTNAME"
            )
        }
        coEvery { coronaTestCensor2.checkLog(any()) } answers {
            val msg = arg<String>(0)
            BugCensor.CensorContainer(msg).censor(
                "qrcode-RAPID_ANTIGEN-9a9a35fa1cf3261be3349fc50a37b58280634bf42487c8e4eca060c48f259eb7",
                "IDENTIFIER"
            )
        }

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        instance.start()

        Timber.tag("Test").v(before)
        advanceTimeBy(2000L)

        val rawWritten = runningLog.readText()
        val cleanedWritten = rawWritten.substring(rawWritten.indexOf("RACoronaTest"))

        cleanedWritten shouldBe after + "\n"

        instance.stop()
        advanceUntilIdle()
    }

    @Test
    fun `censoring collision with larger than original index bounds`() = runTest {
        val before = "shortBefore" // Without timestamp

        coEvery { coronaTestCensor1.checkLog(any()) } answers {
            val msg = arg<String>(0)
            BugCensor.CensorContainer(msg).censor("Before", "After")
        }
        coEvery { coronaTestCensor2.checkLog(any()) } answers {
            val msg = arg<String>(0)
            var orig = BugCensor.CensorContainer(msg)

            orig = orig.censor("ortBef", "thisReallyIsNotShortAnymore")
            orig = orig.censor("Anymore", "Nevermore")
            orig
        }

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        instance.start()

        Timber.tag("Test").v(before)
        advanceTimeBy(2000L)

        runningLog.readLines().last().substring(25) shouldBe "V/Test: sh<censor-collision/>"

        instance.stop()
        advanceUntilIdle()
    }

    // Censoring bounds need to be determined on the original string
    @Test
    fun `censoring collision with missmatching original and replacements`() = runTest {
        val before = "StrawBerryCake" // Without timestamp

        coEvery { coronaTestCensor1.checkLog(any()) } answers {
            val msg = arg<String>(0)
            BugCensor.CensorContainer(msg).censor("Berry", "Banana")
        }
        coEvery { coronaTestCensor2.checkLog(any()) } answers {
            val msg = arg<String>(0)
            var orig = BugCensor.CensorContainer(msg)

            orig = orig.censor("StrawBerry", "StrawBerryBananaPie")
            orig = orig.censor("StrawBerryBananaPie", "Apple")
            orig
        }

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        instance.start()

        Timber.tag("Test").v(before)
        advanceTimeBy(2000L)

        runningLog.readLines().last().substring(25) shouldBe "V/Test: <censor-collision/>Cake"

        instance.stop()
        advanceUntilIdle()
    }

    @Test
    fun `censoring collision without overlap`() = runTest {
        val before = "StrawBerryCakeWithCream" // Without timestamp

        coEvery { coronaTestCensor1.checkLog(any()) } answers {
            val msg = arg<String>(0)
            BugCensor.CensorContainer(msg)
                .censor("Straw", "Strap")
                .censor("With", "More")
        }
        coEvery { coronaTestCensor2.checkLog(any()) } answers {
            val msg = arg<String>(0)
            BugCensor.CensorContainer(msg)
                .censor("Berry", "Barry")
                .censor("Cream", "Sugar")
        }

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        instance.start()

        Timber.tag("Test").v(before)
        advanceTimeBy(2000L)

        runningLog.readLines()
            .last()
            .substring(25) shouldBe "V/Test: StrapBarryCakeMoreSugar"

        instance.stop()
        advanceUntilIdle()
    }

    @Test
    fun `exception during single bugcensor execution`() = runTest {
        val before = "StrawberryCake" // Without timestamp

        coEvery { coronaTestCensor1.checkLog(any()) } answers {
            null
        }
        coEvery { coronaTestCensor2.checkLog(any()) } answers {
            throw IllegalArgumentException("I give up")
        }

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        instance.start()

        Timber.tag("Test").v(before)
        advanceTimeBy(2000L)

        runningLog.readLines()
            .last()
            .substring(25).apply {
                this shouldStartWith "V/Test: <censor-error>Module BugCensor"
                this shouldEndWith "lang.IllegalArgumentException: I give up</censor-error>"
            }

        instance.stop()
        advanceUntilIdle()
    }

    @Test
    fun `exception during multi bugcensor execution`() = runTest {
        val before = "StrawberryCake" // Without timestamp

        coEvery { coronaTestCensor1.checkLog(any()) } answers {
            val msg = arg<String>(0)
            BugCensor.CensorContainer(msg).censor("Cake", "Pancake")
        }
        coEvery { coronaTestCensor2.checkLog(any()) } answers {
            throw IllegalArgumentException("I give up")
        }

        val instance = createInstance(scope = this).apply {
            init()
            setInjectionIsReady(component)
        }

        instance.start()

        Timber.tag("Test").v(before)
        advanceTimeBy(2000L)

        runningLog.readLines()
            .last()
            .substring(25) shouldBe "V/Test: <censor-collision/>"

        instance.stop()
        advanceUntilIdle()
    }
}
