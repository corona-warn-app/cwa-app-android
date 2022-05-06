@file:Suppress("DEPRECATION")

package de.rki.coronawarnapp.util.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.BuildVersionWrap
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.coroutines.test
import testhelpers.preferences.mockFlowPreference

class NetworkStateProviderTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var connectivityManager: ConnectivityManager
    @MockK lateinit var testSettings: TestSettings

    @MockK lateinit var network: Network
    @MockK lateinit var networkInfo: NetworkInfo
    @MockK lateinit var networkRequest: NetworkRequest
    @MockK lateinit var networkRequestBuilder: NetworkRequest.Builder
    @MockK lateinit var networkRequestBuilderProvider: NetworkRequestBuilderProvider
    @MockK lateinit var capabilities: NetworkCapabilities
    @MockK lateinit var linkProperties: LinkProperties

    private var lastRequest: NetworkRequest? = null
    private var lastCallback: ConnectivityManager.NetworkCallback? = null

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(BuildVersionWrap)
        every { BuildVersionWrap.SDK_INT } returns 24

        every { testSettings.fakeMeteredConnection } returns mockFlowPreference(false)
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager

        every { networkRequestBuilderProvider.get() } returns networkRequestBuilder
        networkRequestBuilder.apply {
            every { addCapability(any()) } returns networkRequestBuilder
            every { build() } returns networkRequest
        }

        connectivityManager.apply {
            every { activeNetwork } returns network
            every { activeNetworkInfo } answers { networkInfo }
            every { unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just Runs

            every { getNetworkCapabilities(network) } answers { capabilities }
            every { getLinkProperties(network) } answers { linkProperties }

            every {
                registerNetworkCallback(any<NetworkRequest>(), any<ConnectivityManager.NetworkCallback>())
            } answers {
                lastRequest = arg(0)
                lastCallback = arg(1)
                mockk()
            }
        }

        networkInfo.apply {
            every { type } returns ConnectivityManager.TYPE_WIFI
            every { isConnected } returns true
        }

        capabilities.apply {
            // The happy path is an unmetered internet connection being available
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        }
    }

    private fun createInstance(scope: CoroutineScope) = NetworkStateProvider(
        context = context,
        appScope = scope,
        networkRequestBuilderProvider = networkRequestBuilderProvider,
        testSettings = testSettings
    )

    @Test
    fun `init is side effect free and lazy`() = runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
        shouldNotThrowAny {
            createInstance(this)
        }
        verify { connectivityManager wasNot Called }
    }

    @Test
    fun `initial state is emitted correctly without callback`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val instance = createInstance(this)

            instance.networkState.first().apply {
                isMeteredConnection shouldBe false
                isInternetAvailable shouldBe true
            }

            advanceUntilIdle()

            verifySequence {
                connectivityManager.activeNetwork
                connectivityManager.getNetworkCapabilities(network)
                connectivityManager.registerNetworkCallback(networkRequest, any<ConnectivityManager.NetworkCallback>())
                connectivityManager.unregisterNetworkCallback(lastCallback!!)
            }
        }

    @Test
    fun `we can handle null networks`() = runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
        every { connectivityManager.activeNetwork } returns null
        val instance = createInstance(this)

        instance.networkState.first().apply {
            isInternetAvailable shouldBe false
            isMeteredConnection shouldBe true
        }
        verify { connectivityManager.activeNetwork }
    }

    @Test
    fun `system callbacks lead to new emissions with an updated state`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val instance = createInstance(this)

            val testCollector = instance.networkState.test(startOnScope = this)

            lastCallback!!.onAvailable(mockk())

            every { connectivityManager.activeNetwork } returns null
            lastCallback!!.onUnavailable()

            every { connectivityManager.activeNetwork } returns network
            lastCallback!!.onAvailable(mockk())

            advanceUntilIdle()

            // 3 not 4 as first onAvailable call doesn't change the value (stateIn behavior)
            testCollector.latestValues.size shouldBe 3

            testCollector.awaitFinal(cancel = true)

            verifySequence {
                // Start value
                connectivityManager.activeNetwork
                connectivityManager.getNetworkCapabilities(network)
                connectivityManager.registerNetworkCallback(networkRequest, any<ConnectivityManager.NetworkCallback>())

                // onAvailable
                connectivityManager.activeNetwork
                connectivityManager.getNetworkCapabilities(network)

                // onUnavailable
                connectivityManager.activeNetwork

                // onAvailable
                connectivityManager.activeNetwork
                connectivityManager.getNetworkCapabilities(network)
                connectivityManager.unregisterNetworkCallback(lastCallback!!)
            }
        }

    @Test
    fun `metered connection state checks capabilities`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            createInstance(this).apply {
                networkState.first().isMeteredConnection shouldBe false

                every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } returns false
                networkState.first().isMeteredConnection shouldBe true

                every { connectivityManager.getNetworkCapabilities(any()) } returns null
                networkState.first().isMeteredConnection shouldBe true
            }
        }

    @Test
    fun `metered connection state can be overridden via test settings`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            val instance = createInstance(this)

            instance.networkState.first().isMeteredConnection shouldBe false

            every { testSettings.fakeMeteredConnection } returns mockFlowPreference(true)

            instance.networkState.first().isMeteredConnection shouldBe true
        }

    @Test
    fun `Android 6 not metered on wifi`() = runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
        every { BuildVersionWrap.SDK_INT } returns 23
        val instance = createInstance(this)

        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        instance.networkState.first().isMeteredConnection shouldBe true

        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        instance.networkState.first().isMeteredConnection shouldBe false

        every { connectivityManager.getNetworkCapabilities(any()) } returns null
        instance.networkState.first().isMeteredConnection shouldBe true
    }

    @Test
    fun `if we fail to register the callback, we do not attempt to unregister it`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            every {
                connectivityManager.registerNetworkCallback(
                    any(),
                    any<ConnectivityManager.NetworkCallback>()
                )
            } throws SecurityException()

            val instance = createInstance(this)

            instance.networkState.first().apply {
                isInternetAvailable shouldBe true
                isMeteredConnection shouldBe true
            }

            advanceUntilIdle()

            verifySequence {
                connectivityManager.activeNetwork
                connectivityManager.getNetworkCapabilities(network)
                connectivityManager.registerNetworkCallback(networkRequest, any<ConnectivityManager.NetworkCallback>())
            }
            verify(exactly = 0) {
                connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())
            }
        }

    @Test
    fun `current state is correctly determined below API 23`() =
        runTest2(ignoreActive = true, context = UnconfinedTestDispatcher()) {
            every { BuildVersionWrap.SDK_INT } returns 22

            createInstance(this).apply {
                networkState.first().apply {
                    isInternetAvailable shouldBe true
                    isMeteredConnection shouldBe false
                }

                every { networkInfo.type } returns ConnectivityManager.TYPE_MOBILE
                networkState.first().apply {
                    isInternetAvailable shouldBe true
                    isMeteredConnection shouldBe true
                }

                every { networkInfo.isConnected } returns false
                networkState.first().apply {
                    isInternetAvailable shouldBe false
                    isMeteredConnection shouldBe true
                }
            }

            verify { connectivityManager.activeNetworkInfo }
        }
}
