package de.rki.coronawarnapp.nearby.modules.tracing

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask

class DefaultTracingStatusTest : BaseTest() {

    @MockK lateinit var client: ExposureNotificationClient

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { client.isEnabled } returns MockGMSTask.forValue(true)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): DefaultTracingStatus = DefaultTracingStatus(
        client = client
    )

    @Test
    fun `init is sideeffect free and lazy`() {
        createInstance()
        verify { client wasNot Called }
    }

    @Test
    fun `state emission works`() = runBlockingTest {
        val instance = createInstance()
        instance.isTracingEnabled.first() shouldBe true
    }

    @Test
    fun `state is updated and polling stops on collection stop`() = runBlockingTest {
        every { client.isEnabled } returnsMany listOf(
            true, false, true, false, true, false, true
        ).map { MockGMSTask.forValue(it) }

        val instance = createInstance()

        instance.isTracingEnabled.take(6).toList() shouldBe listOf(
            true, false, true, false, true, false
        )
        verify(exactly = 6) { client.isEnabled }
    }
}
