package de.rki.coronawarnapp.risk.storage.legacy

import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class RiskLevelResultMigratorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    val mockPreferences = MockSharedPreferences()

    fun createInstance() = RiskLevelResultMigrator(
        timeStamper = timeStamper,
        encryptedPreferences = { mockPreferences }
    )

    @Test
    fun `empty list if no previous data was available`() {
        TODO()
    }

    @Test
    fun `if no timestamp is available we use the current time`() {
        TODO()
    }

    @Test
    fun `either last successful or just last calculated values can be null`() {
        TODO()
    }
}
