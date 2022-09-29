package de.rki.coronawarnapp.nearby.modules.exposurewindow

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask

class ExposureWindowProviderTest : BaseTest() {
    @MockK lateinit var googleENFClient: ExposureNotificationClient

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { googleENFClient.exposureWindows } returns MockGMSTask.forValue(emptyList())
    }

    private fun createProvider() = DefaultExposureWindowProvider(
        client = googleENFClient
    )

    @Test
    fun `fake exposure windows only in tester builds`() {
        createProvider()
        CWADebug.isDeviceForTestersBuild shouldBe false
    }
}
