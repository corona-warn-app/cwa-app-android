package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.permission

import android.content.Context
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.permission.CameraSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class CameraPermissionProviderTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var cameraSettings: CameraSettings

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CameraPermissionHelper)
    }

    @Test
    fun `App fresh state`() = runBlockingTest {
        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(false)
        every { CameraPermissionHelper.hasCameraPermission(any()) } returns false

        cameraPermissionProvider().deniedPermanently.first() shouldBe false
    }

    @Test
    fun `User denied permanently`() = runBlockingTest {
        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(true)
        every { CameraPermissionHelper.hasCameraPermission(any()) } returns false

        cameraPermissionProvider().deniedPermanently.first() shouldBe true
    }

    @Test
    fun `User denied permanently then enabled from device settings`() = runBlockingTest {
        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(true)
        every { CameraPermissionHelper.hasCameraPermission(any()) } returns true

        cameraPermissionProvider().deniedPermanently.first() shouldBe false
    }

    private fun cameraPermissionProvider() = CameraPermissionProvider(context, cameraSettings)
}