package de.rki.coronawarnapp.main

import android.content.Context
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.main.CWASettings.Companion.PKEY_DEVICE_TIME_FIRST_RELIABLE
import de.rki.coronawarnapp.main.CWASettings.Companion.PKEY_DEVICE_TIME_INCORRECT_ACK
import de.rki.coronawarnapp.main.CWASettings.Companion.PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE
import de.rki.coronawarnapp.main.CWASettings.Companion.PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore
import kotlinx.coroutines.test.runTest
import java.time.Instant

class CWASettingsTest : BaseTest() {

    @MockK lateinit var context: Context
    private val fakeDataStore = FakeDataStore()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    fun createInstance() = CWASettings(dataStore = fakeDataStore)

    @Test
    fun `update incorrect time acknowledgement`() = runTest {
        createInstance().apply {
            fakeDataStore[PKEY_DEVICE_TIME_INCORRECT_ACK] shouldBe null
            wasDeviceTimeIncorrectAcknowledged.first() shouldBe false
            updateWasDeviceTimeIncorrectAcknowledged(true)
            fakeDataStore[PKEY_DEVICE_TIME_INCORRECT_ACK] shouldBe true

            updateWasDeviceTimeIncorrectAcknowledged(false)
            fakeDataStore[PKEY_DEVICE_TIME_INCORRECT_ACK] shouldBe false
        }
    }

    @Test
    fun `update first reliable device time`() = runTest {
        createInstance().apply {
            fakeDataStore[PKEY_DEVICE_TIME_FIRST_RELIABLE] shouldBe null

            firstReliableDeviceTime.first() shouldBe Instant.EPOCH
            updateFirstReliableDeviceTime(Instant.ofEpochMilli(12345L))
            fakeDataStore[PKEY_DEVICE_TIME_FIRST_RELIABLE] shouldBe 12345L

            updateFirstReliableDeviceTime(Instant.ofEpochMilli(9999L))
            fakeDataStore[PKEY_DEVICE_TIME_FIRST_RELIABLE] shouldBe 9999L
        }
    }

    @Test
    fun `update last device time state change timestamp`() = runTest {
        createInstance().apply {
            fakeDataStore[PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME] shouldBe null

            lastDeviceTimeStateChangeAt.first() shouldBe Instant.EPOCH
            updateLastDeviceTimeStateChangeAt(Instant.ofEpochMilli(1111L))
            fakeDataStore[PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME] shouldBe 1111L

            updateLastDeviceTimeStateChangeAt(Instant.ofEpochMilli(2222L))
            fakeDataStore[PKEY_DEVICE_TIME_LAST_STATE_CHANGE_TIME] shouldBe 2222L
        }
    }

    @Test
    fun `update last device time state change state`() = runTest {
        createInstance().apply {
            fakeDataStore[PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE] shouldBe null

            lastDeviceTimeStateChangeState.first() shouldBe ConfigData.DeviceTimeState.INCORRECT
            updateLastDeviceTimeStateChangeState(ConfigData.DeviceTimeState.CORRECT)
            fakeDataStore[PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE] shouldBe "CORRECT"

            updateLastDeviceTimeStateChangeState(ConfigData.DeviceTimeState.ASSUMED_CORRECT)
            fakeDataStore[PKEY_DEVICE_TIME_LAST_STATE_CHANGE_STATE] shouldBe "ASSUMED_CORRECT"
        }
    }
}
