package de.rki.coronawarnapp.tracing

import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.bluetooth.BluetoothProvider
import de.rki.coronawarnapp.util.location.LocationProvider
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.coroutines.test
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class GeneralTracingStatusTest : BaseTest() {

    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var bluetoothProvider: BluetoothProvider
    @MockK lateinit var locationProvider: LocationProvider

    private val isBluetoothEnabled = MutableStateFlow(true)
    private val isTracingEnabled = MutableStateFlow(true)
    private val isLocationEnabled = MutableStateFlow(true)
    private val isLocationLessScanningSupported = MutableStateFlow(true)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { bluetoothProvider.isBluetoothEnabled } returns isBluetoothEnabled
        every { enfClient.isTracingEnabled } returns isTracingEnabled
        every { locationProvider.isLocationEnabled } returns isLocationEnabled
        every { enfClient.isLocationLessScanningSupported } returns isLocationLessScanningSupported
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): GeneralTracingStatus = GeneralTracingStatus(
        bluetoothProvider = bluetoothProvider,
        locationProvider = locationProvider,
        enfClient = enfClient
    )

    @Test
    fun `flow updates work`() = runBlockingTest {
        val testCollector = createInstance().generalStatus.test(startOnScope = this)

        isBluetoothEnabled.emit(false)
        isBluetoothEnabled.emit(true)

        advanceUntilIdle()

        testCollector.latestValues shouldBe listOf(
            GeneralTracingStatus.Status.TRACING_ACTIVE,
            GeneralTracingStatus.Status.BLUETOOTH_DISABLED,
            GeneralTracingStatus.Status.TRACING_ACTIVE
        )

        testCollector.cancel()
    }

    @Test
    fun `bluetooth state change`() = runBlockingTest {
        createInstance().generalStatus.first() shouldBe GeneralTracingStatus.Status.TRACING_ACTIVE
        isBluetoothEnabled.emit(false)
        createInstance().generalStatus.first() shouldBe GeneralTracingStatus.Status.BLUETOOTH_DISABLED
    }

    @Test
    fun `tracing state change`() = runBlockingTest {
        createInstance().generalStatus.first() shouldBe GeneralTracingStatus.Status.TRACING_ACTIVE
        isTracingEnabled.emit(false)
        createInstance().generalStatus.first() shouldBe GeneralTracingStatus.Status.TRACING_INACTIVE
    }

    @Test
    fun `location state change`() = runBlockingTest {
        createInstance().generalStatus.first() shouldBe GeneralTracingStatus.Status.TRACING_ACTIVE

        isLocationEnabled.emit(false)
        createInstance().generalStatus.first() shouldBe GeneralTracingStatus.Status.TRACING_ACTIVE

        isLocationLessScanningSupported.emit(false)
        createInstance().generalStatus.first() shouldBe GeneralTracingStatus.Status.LOCATION_DISABLED

        isLocationEnabled.emit(true)
        createInstance().generalStatus.first() shouldBe GeneralTracingStatus.Status.TRACING_ACTIVE
    }
}
