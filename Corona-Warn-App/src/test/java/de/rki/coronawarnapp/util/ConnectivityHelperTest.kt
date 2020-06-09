package de.rki.coronawarnapp.util

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkRequest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * ConnectivityHelper test.
 */
class ConnectivityHelperTest {

    @MockK
    private lateinit var bAdapter: BluetoothAdapter

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(BluetoothAdapter::class)
    }

    /**
     * Test network callback behavior.
     */
    @Test
    fun testNetworkCallback() {
        var registered: Boolean? = null
        var available: Boolean? = null
        val callback = object : ConnectivityHelper.NetworkCallback() {
            override fun onNetworkAvailable() {
                available = true
            }

            override fun onNetworkUnavailable() {
                available = false
            }
        }
        mockkConstructor(NetworkRequest.Builder::class)
        mockkObject(NetworkRequest.Builder())
        val request = mockk<NetworkRequest>()
        val manager = mockk<ConnectivityManager>()
        every {
            anyConstructed<NetworkRequest.Builder>().addCapability(any()).addCapability(any())
                .build()
        } returns request
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns manager
        every { manager.registerNetworkCallback(any(), callback) } answers { registered = true }
        every { manager.unregisterNetworkCallback(callback) } answers { registered = false }

        // register
        ConnectivityHelper.registerNetworkStatusCallback(context, callback)

        assertEquals(registered, true)
        assertEquals(available, false)

        // network found
        callback.onAvailable(mockk())
        assertEquals(available, true)

        // loss of network
        callback.onLost(mockk())
        assertEquals(available, false)

        // unregister
        ConnectivityHelper.unregisterNetworkStatusCallback(context, callback)
        assertEquals(registered, false)
    }

    /**
     * Test bluetooth callback behaviour.
     */
    @Test
    fun testBluetoothCallback() {
        var registered: Boolean? = null
        var available: Boolean? = null
        val callback = object : ConnectivityHelper.BluetoothCallback() {
            override fun onBluetoothAvailable() {
                available = true
            }

            override fun onBluetoothUnavailable() {
                available = false
            }
        }

        val turnOn = mockk<Intent>()
        every { turnOn.action } returns BluetoothAdapter.ACTION_STATE_CHANGED
        every {
            turnOn.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                any()
            )
        } returns BluetoothAdapter.STATE_ON

        val turnOff = mockk<Intent>()
        every { turnOff.action } returns BluetoothAdapter.ACTION_STATE_CHANGED
        every {
            turnOff.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                any()
            )
        } returns BluetoothAdapter.STATE_OFF

        every { BluetoothAdapter.getDefaultAdapter() } returns bAdapter
        every { context.registerReceiver(any(), any()) } answers {
            registered = true
            mockk()
        }
        every { context.unregisterReceiver(any()) } answers { registered = false }

        // turned on
        every { bAdapter.isEnabled } returns true

        // register
        ConnectivityHelper.registerBluetoothStatusCallback(context, callback)

        assertNotNull(callback.recevier)
        assertEquals(registered, true)
        assertEquals(available, true)

        // turned off
        callback.recevier?.onReceive(context, turnOff)
        assertEquals(available, false)

        // turned on
        callback.recevier?.onReceive(context, turnOn)
        assertEquals(available, true)

        // unregister
        ConnectivityHelper.unregisterBluetoothStatusCallback(context, callback)

        assertNull(callback.recevier)
        assertEquals(registered, false)
    }

    /**
     * Test bluetooth state.
     */
    @Test
    fun testBluetoothState() {
        // no bluetooth hardware
        assertFalse(ConnectivityHelper.isBluetoothEnabled())

        every { BluetoothAdapter.getDefaultAdapter() } returns bAdapter

        // check turned off
        every { bAdapter.isEnabled } returns false
        assertFalse(ConnectivityHelper.isBluetoothEnabled())

        // check turned on
        every { bAdapter.isEnabled } returns true
        assertTrue(ConnectivityHelper.isBluetoothEnabled())
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
