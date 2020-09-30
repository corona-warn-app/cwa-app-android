package de.rki.coronawarnapp.util.security

import android.content.Context
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class EncryptedPreferencesFactoryTest : BaseTest() {
    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `sideeffect free init`() {
        shouldNotThrowAny {
            EncryptedPreferencesFactory(context)
        }
        verify { context.getSharedPreferences(any(), any()) wasNot Called }
    }
}
