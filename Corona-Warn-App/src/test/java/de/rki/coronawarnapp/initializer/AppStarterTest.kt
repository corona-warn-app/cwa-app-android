package de.rki.coronawarnapp.initializer

import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import de.rki.coronawarnapp.DaggerInitializersTestComponent
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryRetentionCalculation
import de.rki.coronawarnapp.eol.AppEol
import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpScheduler
import io.github.classgraph.ClassGraph
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainAll
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

class AppStarterTest : BaseTest() {

    @MockK lateinit var appEol: AppEol
    @MockK lateinit var notificationManager: NotificationManagerCompat
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var recycleBinCleanUpScheduler: RecycleBinCleanUpScheduler
    @MockK lateinit var contactDiaryRetentionCalculation: ContactDiaryRetentionCalculation

    private val initializers = DaggerInitializersTestComponent.create().initializers

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { appEol.isEol } returns flowOf(false)
        coEvery { contactDiaryRetentionCalculation.clearOutdatedEntries() } just Runs
        coEvery { recycleBinCleanUpScheduler.initialize() } just Runs
        coEvery { notificationManager.cancelAll() } just Runs
        coEvery { workManager.cancelAllWork() } returns mockk()
    }

    @Test
    fun `initializers are working`() = runTest {
        instance(this).start()
        advanceUntilIdle()

        val scanResult = ClassGraph()
            .acceptPackages("de.rki.coronawarnapp")
            .enableClassInfo()
            .scan()

        val initializersClasses = scanResult
            .getClassesImplementing(Initializer::class.java)
            .filterNot { it.isAbstract }

        println("initializersClasses [${initializersClasses.size}]")
        val injected = initializers.map { it::class.simpleName }.toSet()
        val existing = initializersClasses.map { it.simpleName }.toSet()
        injected shouldContainAll existing

        coVerify {
            initializers.forEach { initializer -> initializer.initialize() }
        }
    }

    @Test
    fun `App EOL is activated`() = runTest {
        every { appEol.isEol } returns flowOf(true)
        instance(this).start()
        advanceUntilIdle()

        coVerify {
            contactDiaryRetentionCalculation.clearOutdatedEntries()
            workManager.cancelAllWork()
            notificationManager.cancelAll()
            recycleBinCleanUpScheduler.initialize()
        }
    }

    @Test
    fun `App EOL is activated - no crash`() = runTest {
        every { appEol.isEol } returns flowOf(true)
        coEvery { contactDiaryRetentionCalculation.clearOutdatedEntries() } throws Exception("Surprise!!!")
        shouldNotThrowAny {
            instance(this).start()
            advanceUntilIdle()
        }
    }

    private fun instance(scope: CoroutineScope) = AppStarter(
        appEol = appEol,
        workManager = workManager,
        initializers = initializers,
        notificationManager = notificationManager,
        contactDiaryRetentionCalculation = contactDiaryRetentionCalculation,
        recycleBinCleanUpScheduler = recycleBinCleanUpScheduler,
        appScope = scope
    )
}
