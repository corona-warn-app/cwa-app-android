package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.test

class CWABluetoothTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var bluetoothAdapter: BluetoothAdapter
    private val appScope: CoroutineScope = TestCoroutineScope()
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

        every { bluetoothAdapter.isEnabled } returns true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(
        scope: CoroutineScope = appScope,
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
    fun `init is sideeffect free and lazy`() {
        createInstance()
        verify { context wasNot Called }
        verify { bluetoothAdapter wasNot Called }
    }

    @Test
    fun `initial state is emitted correctly without callback`() = runBlockingTest {
        val instance = createInstance()
        instance.isBluetoothEnabled.first() shouldBe true

        verifySequence {
            bluetoothAdapter.isEnabled
            context.registerReceiver(any(), any())
            context.unregisterReceiver(receiverSlot.captured)
        }
    }

    @Test
    fun `system callbacks lead to new emissions with an updated state`() = runBlockingTest {
        val instance = createInstance()

        val testCollector = instance.isBluetoothEnabled.test(scope = this)

        lastFilter!!.hasAction(BluetoothAdapter.ACTION_STATE_CHANGED)

        lastReceiver!!.apply {
            onReceive(mockk(), mockBluetoothIntent(enabled = false))
            onReceive(mockk(), mockBluetoothIntent(enabled = true))
            onReceive(mockk(), mockBluetoothIntent(enabled = null))
        }

        testCollector.values() shouldBe listOf(true, false, true)

        instance.isBluetoothEnabled.first() shouldBe true

        testCollector.cancel()
    }

    @Test
    fun `null adapter defaults to false`() = runBlockingTest {
        val instance = createInstance(adapter = null)
        instance.isBluetoothEnabled.first() shouldBe false
    }
}
