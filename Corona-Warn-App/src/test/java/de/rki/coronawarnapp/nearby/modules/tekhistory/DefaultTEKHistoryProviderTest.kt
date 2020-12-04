package de.rki.coronawarnapp.nearby.modules.tekhistory

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask

class DefaultTEKHistoryProviderTest : BaseTest() {

    @MockK lateinit var client: ExposureNotificationClient

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { client.temporaryExposureKeyHistory } answers { MockGMSTask.forValue(listOf(mockk())) }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = DefaultTEKHistoryProvider(
        client = client
    )

    @Test
    fun `init is sideeffect free and lazy`() = runBlockingTest {
        createInstance()

        advanceUntilIdle()

        verify { client wasNot Called }
    }

    @Test
    fun `history is forwarded`() = runBlockingTest {
        val instance = createInstance()
        instance.getTEKHistory().size shouldBe 1
    }

    @Test
    fun `errors are forwarded`() = runBlockingTest {
        val error = ApiException(Status.RESULT_DEAD_CLIENT)
        every { client.temporaryExposureKeyHistory } answers { MockGMSTask.forError(error) }
        val instance = createInstance()

        shouldThrowExactly<ApiException> { instance.getTEKHistory() } shouldBe error
    }
}
