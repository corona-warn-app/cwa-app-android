package de.rki.coronawarnapp.storage

import android.content.Context
import de.rki.coronawarnapp.util.CWADebug
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
}
