package de.rki.coronawarnapp.util.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import de.rki.coronawarnapp.storage.TestSettings
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.coroutines.test
import testhelpers.preferences.mockFlowPreference

class NetworkStateProviderTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var conMan: ConnectivityManager
    @MockK lateinit var testSettings: TestSettings

    @MockK lateinit var network: Network
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

        every {
            conMan.registerNetworkCallback(
                any<NetworkRequest>(),
                any<ConnectivityManager.NetworkCallback>()
            )
        } answers {
            lastRequest = arg(0)
            lastCallback = arg(1)
            mockk()
        }
        every { conMan.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just Runs

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns conMan

        every { networkRequestBuilderProvider.get() } returns networkRequestBuilder
        every { networkRequestBuilder.addCapability(any()) } returns networkRequestBuilder
        every { networkRequestBuilder.build() } returns networkRequest

        every { conMan.activeNetwork } returns network
        every { conMan.getNetworkCapabilities(network) } returns capabilities
        every { conMan.getLinkProperties(network) } returns linkProperties

        every { testSettings.fakeMeteredConnection } returns mockFlowPreference(false)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(scope: CoroutineScope) = NetworkStateProvider(
        context = context,
        appScope = scope,
        networkRequestBuilderProvider = networkRequestBuilderProvider,
        testSettings = testSettings
    )

    @Test
    fun `init is sideeffect free and lazy`() {
        shouldNotThrowAny {
            createInstance(TestCoroutineScope())
        }
        verify { conMan wasNot Called }
    }

    @Test
    fun `initial state is emitted correctly without callback`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        instance.networkState.first() shouldBe NetworkStateProvider.State(
            activeNetwork = network,
            capabilities = capabilities,
            linkProperties = linkProperties
        )

        advanceUntilIdle()

        verifySequence {
            conMan.activeNetwork
            conMan.getNetworkCapabilities(network)
            conMan.getLinkProperties(network)
            conMan.registerNetworkCallback(networkRequest, any<ConnectivityManager.NetworkCallback>())
            conMan.unregisterNetworkCallback(lastCallback!!)
        }
    }

    @Test
    fun `we can handle null networks`() = runBlockingTest2(ignoreActive = true) {
        every { conMan.activeNetwork } returns null
        val instance = createInstance(this)

        instance.networkState.first() shouldBe NetworkStateProvider.State(
            activeNetwork = null,
            capabilities = null,
            linkProperties = null
        )
        verify { conMan.activeNetwork }
    }

    @Test
    fun `system callbacks lead to new emissions with an updated state`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        val testCollector = instance.networkState.test(startOnScope = this)

        lastCallback!!.onAvailable(mockk())

        every { conMan.activeNetwork } returns null
        lastCallback!!.onUnavailable()

        every { conMan.activeNetwork } returns network
        lastCallback!!.onAvailable(mockk())

        advanceUntilIdle()

        // 3 not 4 as first onAvailable call doesn't change the value (stateIn behavior)
        testCollector.latestValues.size shouldBe 3

        testCollector.awaitFinal(cancel = true)

        verifySequence {
            // Start value
            conMan.activeNetwork
            conMan.getNetworkCapabilities(network)
            conMan.getLinkProperties(network)
            conMan.registerNetworkCallback(networkRequest, any<ConnectivityManager.NetworkCallback>())

            // onAvailable
            conMan.activeNetwork
            conMan.getNetworkCapabilities(network)
            conMan.getLinkProperties(network)

            // onUnavailable
            conMan.activeNetwork

            // onAvailable
            conMan.activeNetwork
            conMan.getNetworkCapabilities(network)
            conMan.getLinkProperties(network)

            conMan.unregisterNetworkCallback(lastCallback!!)
        }
    }

    @Test
    fun `metered connection state checks capabilities`() {
        val capabilities = mockk<NetworkCapabilities>()

        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } returns true
        NetworkStateProvider.State(
            activeNetwork = null,
            capabilities = capabilities,
            linkProperties = null
        ).isMeteredConnection shouldBe false

        NetworkStateProvider.State(
            activeNetwork = null,
            capabilities = null,
            linkProperties = null
        ).isMeteredConnection shouldBe true

        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } returns false
        NetworkStateProvider.State(
            activeNetwork = null,
            capabilities = capabilities,
            linkProperties = null
        ).isMeteredConnection shouldBe true
    }

    @Test
    fun `metered connection state can be overriden via test settings`() = runBlockingTest2(ignoreActive = true) {
        every { testSettings.fakeMeteredConnection } returns mockFlowPreference(true)
        val instance = createInstance(this)

        instance.networkState.first()

        NetworkStateProvider.State(
            activeNetwork = null,
            capabilities = null,
            linkProperties = null
        ).isMeteredConnection shouldBe true
    }
}
