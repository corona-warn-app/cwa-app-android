package de.rki.coronawarnapp

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import coil.ImageLoaderFactory
import dagger.android.DispatchingAndroidInjector
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.github.classgraph.ClassGraph
import io.kotest.matchers.collections.shouldContainAll
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.test.TestScope
import org.conscrypt.Conscrypt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.security.Security

@Disabled
class CoronaWarnApplicationTest : BaseTest() {

    @MockK lateinit var applicationComponent: ApplicationComponent
    @MockK lateinit var androidInjector: DispatchingAndroidInjector<Any>
    @MockK lateinit var taskController: TaskController
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var imageLoaderFactory: ImageLoaderFactory
    @MockK lateinit var context: Context

    lateinit var initializers: Set<Initializer>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(Conscrypt::class)
        mockkStatic(ContextCompat::class)
        every { Conscrypt.newProvider() } returns mockk()

        mockkStatic(Security::class)
        every { Security.insertProviderAt(any(), any()) } returns 0

        mockkObject(CWADebug)
        CWADebug.apply {
            every { init(any()) } just Runs
            every { initAfterInjection(any()) } just Runs
        }

        mockkObject(AppInjector)
        AppInjector.apply {
            every { init(any()) } returns applicationComponent
        }
        applicationComponent.apply {
            every { inject(any<CoronaWarnApplication>()) } answers {
                val app = arg<CoronaWarnApplication>(0)
                app.component = applicationComponent
                app.androidInjector = androidInjector
                app.foregroundState = foregroundState
                app.workManager = workManager
                app.initializers = initializers
                app.appScope = TestScope()
                app.rollingLogHistory = object : Timber.Tree() {
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) = Unit
                }
                app.imageLoaderFactory = imageLoaderFactory
            }
        }
    }

    @Test
    fun `test initializers`() {
        createInstance().onCreate()

        coVerify {
            initializers.forEach { initializer -> initializer.initialize() }
        }

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
    }

    private fun createInstance() = CoronaWarnApplication()
}
