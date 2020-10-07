package de.rki.coronawarnapp.util.security

import android.content.SharedPreferences
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SecurityHelperTest : BaseTest() {
    @MockK
    lateinit var appComponent: ApplicationComponent

    @MockK
    lateinit var errorResetTool: EncryptionErrorResetTool

    @MockK
    lateinit var preferenceFactory: EncryptedPreferencesFactory

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { appComponent.errorResetTool } returns errorResetTool
        every { appComponent.encryptedPreferencesFactory } returns preferenceFactory
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `error free case is sideeffect free`() {
        val sharedPreferences: SharedPreferences = mockk()
        every { preferenceFactory.create("shared_preferences_cwa") } returns sharedPreferences

        SecurityHelper.encryptedPreferencesProvider(appComponent) shouldBe sharedPreferences
        verify(exactly = 0) { errorResetTool.tryResetIfNecessary(any()) }
    }

    @Test
    fun `positive reset tool results cause a retry`() {
        val ourPreferences: SharedPreferences = mockk()
        var ourException: Exception? = null
        every { preferenceFactory.create("shared_preferences_cwa") } answers {
            if (ourException == null) {
                ourException = Exception("99 bugs")
                throw ourException!!
            } else {
                ourPreferences
            }
        }
        every { errorResetTool.tryResetIfNecessary(any()) } returns true

        SecurityHelper.encryptedPreferencesProvider(appComponent) shouldBe ourPreferences

        verifySequence {
            preferenceFactory.create(any())
            errorResetTool.tryResetIfNecessary(ourException!!)
            preferenceFactory.create(any())
        }
    }

    @Test
    fun `negative reset tool results rethrow the exception`() {
        val ourPreferences: SharedPreferences = mockk()
        var ourException: Exception? = null
        every { preferenceFactory.create("shared_preferences_cwa") } answers {
            if (ourException == null) {
                ourException = Exception("99 bugs")
                throw ourException!!
            } else {
                ourPreferences
            }
        }
        every { errorResetTool.tryResetIfNecessary(any()) } returns false

        shouldThrow<Exception> {
            SecurityHelper.encryptedPreferencesProvider(appComponent) shouldBe ourPreferences
        }.cause shouldBe ourException

        verifySequence {
            preferenceFactory.create(any())
            errorResetTool.tryResetIfNecessary(ourException!!)
        }
    }
}
