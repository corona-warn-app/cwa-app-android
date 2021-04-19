package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import de.rki.coronawarnapp.util.ApiLevel
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
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `is scanning supported`() = runBlockingTest {
        TODO()
    }

    @Test
    fun `is advertising supported`() = runBlockingTest {
        TODO()
    }
}
