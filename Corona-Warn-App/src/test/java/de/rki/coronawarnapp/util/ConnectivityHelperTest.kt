package de.rki.coronawarnapp.util

import android.bluetooth.BluetoothAdapter
import android.content.Context
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
import org.junit.Before
import org.junit.Test

/**
 * ConnectivityHelper test.
 */
class ConnectivityHelperTest {

    @MockK private lateinit var context: Context

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

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
