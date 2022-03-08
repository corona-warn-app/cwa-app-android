package de.rki.coronawarnapp.nearby.modules.exposurewindow

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTestInstrumentation
import testhelpers.gms.MockGMSTask

class ExposureWindowProviderTest : BaseTestInstrumentation() {
    @MockK lateinit var googleENFClient: ExposureNotificationClient
    @MockK lateinit var testSettings: TestSettings
    @MockK lateinit var fakeExposureWindowProvider: FakeExposureWindowProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { googleENFClient.exposureWindows } returns MockGMSTask.forValue(emptyList())
    }

    private fun createProvider() = DefaultExposureWindowProvider(
        client = googleENFClient,
        testSettings = testSettings,
        fakeExposureWindowProvider = fakeExposureWindowProvider
    )

    @Test
    fun `fake exposure windows only in tester builds`() {
        createProvider()
        CWADebug.isDeviceForTestersBuild shouldBe true
    }
}
