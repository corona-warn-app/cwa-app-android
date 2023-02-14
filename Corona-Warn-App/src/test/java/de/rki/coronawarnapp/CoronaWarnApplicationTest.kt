package de.rki.coronawarnapp

import androidx.work.WorkManager
import coil.ImageLoaderFactory
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.device.ForegroundState
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
import org.conscrypt.Conscrypt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.Security

class CoronaWarnApplicationTest : BaseTest() {

    @MockK lateinit var taskController: TaskController
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var imageLoaderFactory: ImageLoaderFactory
    private val initializers = DaggerInitializersTestComponent.create().initializers

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(Conscrypt::class)
        every { Conscrypt.newProvider() } returns mockk()

        mockkStatic(Security::class)
        every { Security.insertProviderAt(any(), any()) } returns 0

        mockkObject(CWADebug)
        CWADebug.apply {
            every { init(any()) } just Runs
            every { initAfterInjection(any()) } just Runs
        }
    }

    @Test
    fun `test initializers`() {
        val app = createInstance()
        app.onCreate()

        val scanResult = ClassGraph()
            .acceptPackages("de.rki.coronawarnapp")
            .enableClassInfo()
            .scan()

        val initializersClasses = scanResult
            .getClassesImplementing(Initializer::class.java)
            .filterNot { it.isAbstract }

        println("initializersClasses [${initializersClasses.size}]")
        val injected = app.initializers.get().map { it::class.simpleName }.toSet()
        val existing = initializersClasses.map { it.simpleName }.toSet()
        injected shouldContainAll existing

        coVerify {
            app.initializers.get().forEach { initializer -> initializer.initialize() }
        }
    }

    private fun createInstance() = CoronaWarnApplication()
}
