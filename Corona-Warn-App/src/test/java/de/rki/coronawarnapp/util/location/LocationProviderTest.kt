package de.rki.coronawarnapp.util.location

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.coroutines.test

class LocationProviderTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var locationManager: LocationManager

    private var lastReceiver: BroadcastReceiver? = null
    private var lastFilter: IntentFilter? = null
    private val receiverSlot = slot<BroadcastReceiver>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.registerReceiver(capture(receiverSlot), any()) } answers {
            lastReceiver = arg(0)
            lastFilter = arg(1)
            mockk()
        }
        every { context.unregisterReceiver(any()) } just Runs
        every { context.getSystemService(Context.LOCATION_SERVICE) } returns locationManager
    }

    private fun createInstance(
        scope: CoroutineScope
    ): LocationProvider = LocationProvider(
        context = context,
        appScope = scope
    )

    private fun mockLocationStatus(enabled: Boolean) {
        every { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } returns enabled
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns enabled
    }

    private fun mockLocationChange(enabled: Boolean?): Intent = mockk<Intent>().apply {
        mockLocationStatus(enabled = enabled ?: true)
        every { action } returns "android.location.PROVIDERS_CHANGED"
    }

    @Test
    fun `init is side effect free and lazy`() = runTest2 {
        createInstance(this)
        verify { context wasNot Called }
    }

    @Test
    fun `initial state is emitted correctly without callback`() = runTest2(
        ignoreActive = true, context = UnconfinedTestDispatcher()
    ) {
        mockLocationStatus(true)
        val instance = createInstance(this)
        instance.isLocationEnabled.first() shouldBe true

        verifySequence {
            context.getSystemService(any())
            context.registerReceiver(any(), any())
            context.unregisterReceiver(receiverSlot.captured)
        }
    }

    @Test
    fun `system callbacks lead to new emissions with an updated state`() = runTest2 {
        val instance = createInstance(this)

        mockLocationStatus(enabled = true)

        val testCollector = instance.isLocationEnabled.test(startOnScope = this)

        lastFilter!!.hasAction(BluetoothAdapter.ACTION_STATE_CHANGED)

        lastReceiver!!.apply {
            onReceive(mockk(), mockLocationChange(enabled = false))
            onReceive(mockk(), mockLocationChange(enabled = true))
            onReceive(mockk(), mockLocationChange(enabled = null))
        }

        testCollector.latestValues shouldBe listOf(true, false, true)

        instance.isLocationEnabled.first() shouldBe true

        testCollector.cancel()
    }
}
