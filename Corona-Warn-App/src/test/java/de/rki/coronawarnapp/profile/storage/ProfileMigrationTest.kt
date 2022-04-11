package de.rki.coronawarnapp.profile.storage

import android.content.Context
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettingsDataStore
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProfileMigrationTest {

    @MockK lateinit var context: Context
    @MockK lateinit var setting: RATProfileSettingsDataStore

    lateinit var factory: ProfileDatabase.Factory

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `test migration 1`() {
        createInstance().create()
    }

    private fun createInstance() = ProfileDatabase.Factory(
        context,
        TestCoroutineScope(),
        setting
    )
}
