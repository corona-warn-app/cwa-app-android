package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BluetoothSupportTest : BaseTest() {

    @MockK lateinit var bluetoothAdapter: BluetoothAdapter
    @MockK lateinit var bluetoothLeScanner: BluetoothLeScanner
    @MockK lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_ON
        every { bluetoothAdapter.bluetoothLeScanner } returns null
        every { bluetoothAdapter.bluetoothLeAdvertiser } returns null
    }

    private fun createInstance(
        adapter: BluetoothAdapter? = bluetoothAdapter
    ): BluetoothSupport = BluetoothSupport(
        bluetoothAdapter = adapter
    )

    @Test
    fun `init is side effect free`() {
        createInstance()
    }

    @Test
    fun `scanning not supported without bluetooth`() = runBlockingTest {
        createInstance(null).isScanningSupported shouldBe false
    }

    @Test
    fun `scanning not supported without scanner`() = runBlockingTest {
        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_ON
        every { bluetoothAdapter.bluetoothLeScanner } returns null
        createInstance().isScanningSupported shouldBe false
    }

    @Test
    fun `scanning supported when turned on and has scanner`() = runBlockingTest {
        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_ON
        every { bluetoothAdapter.bluetoothLeScanner } returns bluetoothLeScanner
        createInstance().isScanningSupported shouldBe true
    }

    @Test
    fun `scanning support unknown when turned off`() = runBlockingTest {
        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_OFF
        createInstance().isScanningSupported shouldBe null
    }

    @Test
    fun `advertising not supported`() = runBlockingTest {
        createInstance(null).isAdvertisingSupported shouldBe false
    }

    @Test
    fun `advertising support unknown`() = runBlockingTest {
        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_OFF
        createInstance().isAdvertisingSupported shouldBe null
    }

    @Test
    fun `advertising supported`() = runBlockingTest {
        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_ON
        every { bluetoothAdapter.bluetoothLeAdvertiser } returns bluetoothLeAdvertiser
        createInstance().isAdvertisingSupported shouldBe true
    }
}
