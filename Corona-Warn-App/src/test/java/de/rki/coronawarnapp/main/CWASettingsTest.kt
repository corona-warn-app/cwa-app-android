package de.rki.coronawarnapp.main

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
}
