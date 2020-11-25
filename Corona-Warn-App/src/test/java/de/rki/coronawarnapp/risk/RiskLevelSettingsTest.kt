package de.rki.coronawarnapp.risk

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class RiskLevelSettingsTest : BaseTest() {

    @MockK lateinit var context: Context
    lateinit var preferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        preferences = MockSharedPreferences()
        every { context.getSharedPreferences("risklevel_localdata", Context.MODE_PRIVATE) } returns preferences
    }

    fun createInstance() = RiskLevelSettings(context = context)

    @Test
    fun `update last used config identifier`() {
        createInstance().apply {
            lastUsedConfigIdentifier shouldBe null
            lastUsedConfigIdentifier = "Banana"
            lastUsedConfigIdentifier shouldBe "Banana"
            preferences.dataMapPeek.containsValue("Banana") shouldBe true

            lastUsedConfigIdentifier = null
            lastUsedConfigIdentifier shouldBe null
            preferences.dataMapPeek.isEmpty() shouldBe true
        }
    }
}
