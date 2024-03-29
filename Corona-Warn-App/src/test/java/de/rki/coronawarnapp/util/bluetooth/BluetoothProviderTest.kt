package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

class BluetoothProviderTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var bluetoothAdapter: BluetoothAdapter
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

        every { bluetoothAdapter.isEnabled } returns true
    }

    private fun createInstance(
        scope: CoroutineScope,
        adapter: BluetoothAdapter? = bluetoothAdapter
    ): BluetoothProvider = BluetoothProvider(
        context = context,
        appScope = scope,
        bluetoothAdapter = adapter
    )

    private fun mockBluetoothIntent(enabled: Boolean?): Intent = mockk<Intent>().apply {
        every { action } returns BluetoothAdapter.ACTION_STATE_CHANGED
        every { getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) } returns when (enabled) {
            true -> BluetoothAdapter.STATE_ON
            false -> BluetoothAdapter.STATE_OFF
            else -> Int.MAX_VALUE
        }
    }

    @Test
    fun `init is side effect free and lazy`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            createInstance(this)
            verify { context wasNot Called }
            verify { bluetoothAdapter wasNot Called }
        }

    @Test
    fun `initial state is emitted correctly without callback`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val instance = createInstance(this)
            instance.isBluetoothEnabled.first() shouldBe true

            verifySequence {
                bluetoothAdapter.isEnabled
                context.registerReceiver(any(), any())
                context.unregisterReceiver(receiverSlot.captured)
            }
        }

    @Test
    fun `system callbacks lead to new emissions with an updated state`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val instance = createInstance(this)

            val testCollector = instance.isBluetoothEnabled.test(startOnScope = this)

            lastFilter!!.hasAction(BluetoothAdapter.ACTION_STATE_CHANGED)

            lastReceiver!!.apply {
                onReceive(mockk(), mockBluetoothIntent(enabled = false))
                onReceive(mockk(), mockBluetoothIntent(enabled = true))
                onReceive(mockk(), mockBluetoothIntent(enabled = null))
            }

            testCollector.latestValues shouldBe listOf(true, false, true)

            instance.isBluetoothEnabled.first() shouldBe true

            testCollector.cancel()
        }

    @Test
    fun `null adapter defaults to false`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val instance = createInstance(adapter = null, scope = this)
            instance.isBluetoothEnabled.first() shouldBe false
        }
}
