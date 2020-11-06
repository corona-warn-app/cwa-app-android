package de.rki.coronawarnapp.util.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class NetworkStateProviderTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var locationManager: LocationManager

    private val appScope: CoroutineScope = TestCoroutineScope()
    private var lastReceiver: BroadcastReceiver? = null
    private var lastFilter: IntentFilter? = null
    private val receiverSlot = slot<BroadcastReceiver>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `init is sideeffect free and lazy`() {
        TODO()
    }

    @Test
    fun `initial state is emitted correctly without callback`() = runBlockingTest {
        TODO()
    }

    @Test
    fun `system callbacks lead to new emissions with an updated state`() = runBlockingTest {
        TODO()
    }
}
