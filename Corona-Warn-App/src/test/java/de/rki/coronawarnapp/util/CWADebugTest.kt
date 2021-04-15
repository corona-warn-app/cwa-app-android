package de.rki.coronawarnapp.util

import android.app.Application
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verifySequence
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

class CWADebugTest : BaseTest() {

    @MockK lateinit var application: Application
    @MockK lateinit var appComponent: ApplicationComponent

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { application.cacheDir } returns File("cache")

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.FLAVOR } returns "device"
    }

    @Test
    fun `flavor check`() {
        CWADebug.isDeviceForTestersBuild shouldBe false

        every { BuildConfigWrap.FLAVOR } returns "deviceForTesters"
        CWADebug.isDeviceForTestersBuild shouldBe true
        CWADebug.buildFlavor shouldBe CWADebug.BuildFlavor.DEVICE_FOR_TESTERS

        every { BuildConfigWrap.FLAVOR } returns "device"
        CWADebug.buildFlavor shouldBe CWADebug.BuildFlavor.DEVICE
        CWADebug.isDeviceForTestersBuild shouldBe false
    }

    @Test
    fun `logging is initialized`() {
        val debugLogger = mockk<DebugLogger>().apply {
            every { init() } just Runs
            every { setInjectionIsReady(appComponent) } just Runs
        }

        CWADebug.debugLoggerFactory = { debugLogger }
        CWADebug.init(application)
        CWADebug.initAfterInjection(appComponent)
        verifySequence {
            debugLogger.init()
            debugLogger.setInjectionIsReady(appComponent)
        }
    }
}
