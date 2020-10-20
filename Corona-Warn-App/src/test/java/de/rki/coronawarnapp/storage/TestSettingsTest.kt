package de.rki.coronawarnapp.storage

import android.content.Context
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class TestSettingsTest : BaseTest() {

    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(CWADebug)

        mockPreferences = MockSharedPreferences()
        every {
            context.getSharedPreferences("test_settings", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun buildInstance(): TestSettings = TestSettings(
        context = context
    )

    @Test
    fun `hourly keypkg testing mode`() {
        buildInstance().apply {
            every { CWADebug.isDeviceForTestersBuild } returns true

            isHourKeyPkgMode shouldBe false
            isHourKeyPkgMode = true
            isHourKeyPkgMode shouldBe true
            mockPreferences.dataMapPeek.values.single() shouldBe true

            isHourKeyPkgMode = false
            isHourKeyPkgMode shouldBe false
            mockPreferences.dataMapPeek.values.single() shouldBe false

            isHourKeyPkgMode = true
        }

        buildInstance().apply {
            isHourKeyPkgMode shouldBe true

            // In normal builds this should default to false
            every { CWADebug.isDeviceForTestersBuild } returns false

            isHourKeyPkgMode shouldBe false
        }
    }
}
