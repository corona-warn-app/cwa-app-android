package de.rki.coronawarnapp.util

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes.API_NOT_CONNECTED
import com.google.android.gms.common.api.Status
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GoogleAPIVersionTest {

    private lateinit var classUnderTest: GoogleAPIVersion

    @BeforeEach
    fun setUp() {
        mockkObject(InternalExposureNotificationClient)
        classUnderTest = GoogleAPIVersion()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `isAbove API v16 is true for v17`() {
        coEvery { InternalExposureNotificationClient.getVersion() } returns 17000000L

        runBlockingTest {
            classUnderTest.isAbove(GoogleAPIVersion.V16) shouldBe true
        }

    }

    @ExperimentalCoroutinesApi
    @Test
    fun `isAbove API v16 is false for v15`() {
        coEvery { InternalExposureNotificationClient.getVersion() } returns 15000000L

        runBlockingTest {
            classUnderTest.isAbove(GoogleAPIVersion.V16) shouldBe false
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `isAbove API v16 throws IllegalArgument for invalid version`() {
        assertThrows<IllegalArgumentException> {
            runBlockingTest {
                classUnderTest.isAbove(1L)
            }
            coVerify {
                InternalExposureNotificationClient.getVersion() wasNot Called
            }
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `isAbove API v16 false when APIException for too low version`() {
        coEvery { InternalExposureNotificationClient.getVersion() } throws
                ApiException(Status(API_NOT_CONNECTED))

        runBlockingTest {
            classUnderTest.isAbove(GoogleAPIVersion.V16) shouldBe false
        }
    }

    @AfterEach
    fun teardown() {
        unmockkObject(InternalExposureNotificationClient)
    }
}