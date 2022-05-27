package de.rki.coronawarnapp.main

import android.content.Context
import de.rki.coronawarnapp.appconfig.ConfigData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class CWASettingsTest : BaseTest() {

    @MockK lateinit var context: Context
    lateinit var preferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        preferences = MockSharedPreferences()
        every { context.getSharedPreferences("cwa_main_localdata", Context.MODE_PRIVATE) } returns preferences
    }

    fun createInstance() = CWASettings(context = context)

    @Test
    fun `update incorrect time acknowledgement`() {
        createInstance().apply {
            preferences.dataMapPeek.isEmpty() shouldBe true

            wasDeviceTimeIncorrectAcknowledged shouldBe false
            wasDeviceTimeIncorrectAcknowledged = true
            preferences.dataMapPeek["devicetime.incorrect.acknowledged"] shouldBe true

            wasDeviceTimeIncorrectAcknowledged = false
            preferences.dataMapPeek["devicetime.incorrect.acknowledged"] shouldBe false
        }
    }

    @Test
    fun `update first reliable device time`() {
        createInstance().apply {
            preferences.dataMapPeek.isEmpty() shouldBe true

            firstReliableDeviceTime shouldBe java.time.Instant.EPOCH
            firstReliableDeviceTime = java.time.Instant.ofEpochMilli(12345L)
            preferences.dataMapPeek["devicetime.correct.first"] shouldBe 12345L

            firstReliableDeviceTime = java.time.Instant.ofEpochMilli(9999L)
            preferences.dataMapPeek["devicetime.correct.first"] shouldBe 9999L
        }
    }

    @Test
    fun `update last device time state change timestamp`() {
        createInstance().apply {
            preferences.dataMapPeek.isEmpty() shouldBe true

            lastDeviceTimeStateChangeAt shouldBe Instant.EPOCH
            lastDeviceTimeStateChangeAt = Instant.ofEpochMilli(1111L)
            preferences.dataMapPeek["devicetime.laststatechange.timestamp"] shouldBe 1111L

            lastDeviceTimeStateChangeAt = Instant.ofEpochMilli(2222L)
            preferences.dataMapPeek["devicetime.laststatechange.timestamp"] shouldBe 2222L
        }
    }

    @Test
    fun `update last device time state change state`() {
        createInstance().apply {
            preferences.dataMapPeek.isEmpty() shouldBe true

            lastDeviceTimeStateChangeState shouldBe ConfigData.DeviceTimeState.INCORRECT
            lastDeviceTimeStateChangeState = ConfigData.DeviceTimeState.CORRECT
            preferences.dataMapPeek["devicetime.laststatechange.state"] shouldBe "CORRECT"

            lastDeviceTimeStateChangeState = ConfigData.DeviceTimeState.ASSUMED_CORRECT
            preferences.dataMapPeek["devicetime.laststatechange.state"] shouldBe "ASSUMED_CORRECT"
        }
    }
}
