package de.rki.coronawarnapp.submission.data.tekhistory

import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.TracingPermissionHelper
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TEKHistoryUpdaterTest : BaseTest() {
    @MockK lateinit var tekHistoryStorage: TEKHistoryStorage
    @MockK lateinit var tracingPermissionHelper: TracingPermissionHelper.Factory
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var enfClient: ENFClient

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { enfClient.getTEKHistoryOrRequestPermission(any(), any()) } just Runs
        coEvery { enfClient.isTracingEnabled } returns flowOf(true)
    }

    fun createInstance(scope: CoroutineScope, callback: TEKHistoryUpdater.Callback) = TEKHistoryUpdater(
        callback = callback,
        scope = scope,
        tracingPermissionHelperFactory = tracingPermissionHelper,
        tekHistoryStorage = tekHistoryStorage,
        timeStamper = timeStamper,
        enfClient = enfClient
    )

    @Test
    fun `request is forwaded to enf client`() = runBlockingTest {
        val callback = mockk<TEKHistoryUpdater.Callback>()
        val instance = createInstance(scope = this, callback = callback)

        instance.updateTEKHistoryOrRequestPermission()
        coVerify {
            enfClient.getTEKHistoryOrRequestPermission(
                any(),
                any()
            )
        }
    }
}
