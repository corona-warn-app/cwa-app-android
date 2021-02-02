package de.rki.coronawarnapp.datadonation.analytics.ui

import android.content.Context
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsSettings
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class AnalyticsSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    lateinit var preferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        preferences = MockSharedPreferences()
        every { context.getSharedPreferences("analytics_localdata", Context.MODE_PRIVATE) } returns preferences
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    fun createInstance() = AnalyticsSettings(
        context = context
    )

    @Test
    fun `userinfo agegroup`() {
        TODO()
    }

    @Test
    fun `userinfo federal state`() {
        TODO()
    }

    @Test
    fun `userinfo district`() {
        TODO()
    }
}
