package de.rki.coronawarnapp.nearby.modules.version

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes.API_NOT_CONNECTED
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.gms.MockGMSTask

@ExperimentalCoroutinesApi
internal class DefaultENFVersionTest {

    @MockK lateinit var client: ExposureNotificationClient

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    fun createInstance() = DefaultENFVersion(
        client = client
    )

    @Test
    fun `isAbove API v16 is true for v17`() {
        every { client.version } returns MockGMSTask.forValue(17000000L)

        runBlockingTest {
            createInstance().isAtLeast(ENFVersion.V16) shouldBe true
        }
    }

    @Test
    fun `isAbove API v16 is false for v15`() {
        every { client.version } returns MockGMSTask.forValue(15000000L)

        runBlockingTest {
            createInstance().isAtLeast(ENFVersion.V16) shouldBe false
        }
    }

    @Test
    fun `isAbove API v16 throws IllegalArgument for invalid version`() {
        assertThrows<IllegalArgumentException> {
            runBlockingTest {
                createInstance().isAtLeast(1L)
            }
            verify { client.version wasNot Called }
        }
    }

    @Test
    fun `isAbove API v16 false when APIException for too low version`() {
        every { client.version } returns MockGMSTask.forError(ApiException(Status(API_NOT_CONNECTED)))

        runBlockingTest {
            createInstance().isAtLeast(ENFVersion.V16) shouldBe false
        }
    }

    @Test
    fun `require API v16 throws UnsupportedENFVersionException for v15`() {
        every { client.version } returns MockGMSTask.forValue(ENFVersion.V15)

        assertThrows<ENFVersion.Companion.UnsupportedENFVersionException> {
            runBlockingTest {
                createInstance().requireAtLeast(ENFVersion.V16)
            }
        }
    }

    @Test
    fun `require API v15 does not throw for v16`() {
        every { client.version } returns MockGMSTask.forValue(ENFVersion.V16)

        runBlockingTest {
            createInstance().requireAtLeast(ENFVersion.V15)
        }

        verify { client.version }
    }
}
