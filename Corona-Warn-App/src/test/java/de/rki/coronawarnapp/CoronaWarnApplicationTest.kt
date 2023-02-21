package de.rki.coronawarnapp

import androidx.work.WorkManager
import coil.ImageLoaderFactory
import dagger.Lazy
import de.rki.coronawarnapp.bugreporting.loghistory.LogHistoryTree
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.initializer.AppStarter
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.encryptionmigration.EncryptedPreferencesMigration
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.conscrypt.Conscrypt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.security.Security
import javax.inject.Inject

class CoronaWarnApplicationTest : BaseTest() {

    @MockK lateinit var fState: ForegroundState
    @MockK lateinit var wManager: WorkManager
    @MockK lateinit var imageFactory: ImageLoaderFactory
    @MockK lateinit var starter: AppStarter
    @MockK lateinit var logHistory: Timber.Tree
    @MockK lateinit var encryptedMigration: EncryptedPreferencesMigration

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

        every { starter.start() } returns mockk()
    }

    @Test
    fun `test app starter`() = runTest {
        createInstance().apply {
            appStarter = Lazy { starter }
            appScope = this@runTest
            imageLoaderFactory = imageFactory
            workManager = wManager
            foregroundState = fState
            rollingLogHistory = logHistory
            encryptedPreferencesMigration = Lazy { encryptedMigration }
            onCreate()
        }
        advanceUntilIdle()
        starter.start()
    }

    private fun createInstance() = CoronaWarnApplication()
}
