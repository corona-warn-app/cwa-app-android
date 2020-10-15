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
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.test

class CWALocationTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var locationManager: LocationManager

    private var lastReceiver: BroadcastReceiver? = null
    private var lastFilter: IntentFilter? = null

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.registerReceiver(any(), any()) } answers {
            lastReceiver = arg(0)
            lastFilter = arg(1)
            mockk()
        }
        every { context.getSystemService(Context.LOCATION_SERVICE) } returns locationManager
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): CWALocation = CWALocation(
        context = context
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
    fun `init is sideeffect free and lazy`() {
        createInstance()
        verify { context wasNot Called }
    }

    @Test
    fun `initial state is emitted correctly without callback`() = runBlockingTest {
        mockLocationStatus(true)
        val instance = createInstance()
        instance.isLocationEnabled.first() shouldBe true

        verifySequence {
            context.getSystemService(any())
            context.registerReceiver(any(), any())
        }
    }

    @Test
    fun `system callbacks lead to new emissions with an updated state`() = runBlockingTest {
        val instance = createInstance()

        mockLocationStatus(enabled = true)

        val testCollector = instance.isLocationEnabled.test(scope = this)

        lastFilter!!.hasAction(BluetoothAdapter.ACTION_STATE_CHANGED)

        lastReceiver!!.apply {
            onReceive(mockk(), mockLocationChange(enabled = false))
            onReceive(mockk(), mockLocationChange(enabled = true))
            onReceive(mockk(), mockLocationChange(enabled = null))
        }

        testCollector.values() shouldBe listOf(true, false, true)

        instance.isLocationEnabled.first() shouldBe true

        testCollector.cancel()
    }
}
