package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import de.rki.coronawarnapp.util.ApiLevel
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
    @MockK lateinit var apiLevel: ApiLevel
    @MockK lateinit var bluetoothLeScanner: BluetoothLeScanner

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { apiLevel.hasAPILevel(any()) } returns true
    }

    private fun createInstance(
        adapter: BluetoothAdapter? = bluetoothAdapter
    ): BluetoothSupport = BluetoothSupport(
        apiLevel = apiLevel,
        bluetoothAdapter = adapter
    )

    @Test
    fun `init is side effect free`() {
        createInstance()
    }

    @Test
    fun `is scanning supported`() = runBlockingTest {
        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_ON
        every { bluetoothAdapter.bluetoothLeScanner } returns null
        createInstance().isScanningSupported() shouldBe false

        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_OFF
        every { bluetoothAdapter.bluetoothLeScanner } returns null
        createInstance().isScanningSupported() shouldBe null

        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_ON
        every { bluetoothAdapter.bluetoothLeScanner } returns bluetoothLeScanner
        createInstance().isScanningSupported() shouldBe true

        every { bluetoothAdapter.state } returns BluetoothAdapter.STATE_OFF
        every { bluetoothAdapter.bluetoothLeScanner } returns bluetoothLeScanner
        createInstance().isScanningSupported() shouldBe null
    }

    @Test
    fun `is advertising supported`() = runBlockingTest {
        TODO()
    }
}
