package de.rki.coronawarnapp.nearby.modules.exposurewindow

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask
import java.io.File

class ExposureWindowProviderTest : BaseTest() {
    @MockK lateinit var googleENFClient: ExposureNotificationClient
    @MockK lateinit var testSettings: TestSettings
    @MockK lateinit var fakeExposureWindowProvider: FakeExposureWindowProvider

    private val exampleKeyFiles = listOf(File("file1"), File("file2"))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { googleENFClient.exposureWindows } returns MockGMSTask.forValue(emptyList())
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createProvider() = DefaultExposureWindowProvider(
        client = googleENFClient,
        testSettings = testSettings,
        fakeExposureWindowProvider = fakeExposureWindowProvider
    )

    @Test
    fun `fake exposure windows only in tester builds`() {
        val instance = createProvider()
        CWADebug.isDeviceForTestersBuild shouldBe true
    }
}
