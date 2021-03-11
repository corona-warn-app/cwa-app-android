package de.rki.coronawarnapp.util.security

import android.content.SharedPreferences
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
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

    private val isResetNoticeToBeShown = slot<Boolean>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { appComponent.errorResetTool } returns errorResetTool
        every { appComponent.encryptedPreferencesFactory } returns preferenceFactory
        every { errorResetTool.isResetNoticeToBeShown = capture(isResetNoticeToBeShown) } answers {}
    }

    @Test
    fun `if encrypted preferences exists return it`() {
        val sharedPreferences: SharedPreferences = mockk()
        every { preferenceFactory.create("shared_preferences_cwa") } returns sharedPreferences
        every { errorResetTool.encryptedPreferencesFile.exists() } returns true

        SecurityHelper.encryptedPreferencesProvider(appComponent) shouldBe sharedPreferences
    }

    @Test
    fun `if encrypted preferences exists but are not available return null`() {
        every { preferenceFactory.create("shared_preferences_cwa") } throws Exception()
        every { errorResetTool.encryptedPreferencesFile.exists() } returns true

        SecurityHelper.encryptedPreferencesProvider(appComponent) shouldBe null
        isResetNoticeToBeShown.captured shouldBe true
    }

    @Test
    fun `if encrypted preferences has been already removed return null`() {
        val sharedPreferences: SharedPreferences = mockk()
        every { preferenceFactory.create("shared_preferences_cwa") } returns sharedPreferences
        every { errorResetTool.encryptedPreferencesFile.exists() } returns false

        SecurityHelper.encryptedPreferencesProvider(appComponent) shouldBe null
    }
}
