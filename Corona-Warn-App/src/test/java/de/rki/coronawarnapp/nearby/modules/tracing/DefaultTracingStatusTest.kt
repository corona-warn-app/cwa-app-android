package de.rki.coronawarnapp.nearby.modules.tracing

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.coroutines.test
import testhelpers.gms.MockGMSTask

class DefaultTracingStatusTest : BaseTest() {

    @MockK lateinit var client: ExposureNotificationClient

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { client.isEnabled } answers { MockGMSTask.forValue(true) }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(scope: CoroutineScope): DefaultTracingStatus = DefaultTracingStatus(
        client = client,
        scope = scope
    )

    @Test
    fun `init is sideeffect free and lazy`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this)

        advanceUntilIdle()

        verify { client wasNot Called }
    }

    @Test
    fun `state emission works`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(scope = this)
        instance.isTracingEnabled.first() shouldBe true
    }

    @Test
    fun `state is updated and polling stops on cancel`() = runBlockingTest2(ignoreActive = true) {
        every { client.isEnabled } returnsMany listOf(
            true, false, true, false, true, false, true
        ).map { MockGMSTask.forValue(it) }

        val instance = createInstance(scope = this)

        instance.isTracingEnabled.take(6).toList() shouldBe listOf(
            true, false, true, false, true, false
        )
        verify(exactly = 6) { client.isEnabled }
    }

    @Test
    fun `subscriptions are shared but not cached`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(scope = this)

        val collector1 = instance.isTracingEnabled.test(tag = "1", startOnScope = this)
        val collector2 = instance.isTracingEnabled.test(tag = "2", startOnScope = this)

        delay(500)

        collector1.latestValue shouldBe true
        collector2.latestValue shouldBe true

        collector1.cancel()
        collector2.cancel()

        advanceUntilIdle()

        verify(exactly = 1) { client.isEnabled }

        every { client.isEnabled } answers { MockGMSTask.forValue(false) }
        instance.isTracingEnabled.first() shouldBe false
    }
}
