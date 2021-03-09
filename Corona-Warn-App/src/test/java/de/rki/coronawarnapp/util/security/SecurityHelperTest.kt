package de.rki.coronawarnapp.util.security

import android.content.SharedPreferences
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
    fun `error free case is sideeffect free`() {
        val sharedPreferences: SharedPreferences = mockk()
        every { preferenceFactory.create("shared_preferences_cwa") } returns sharedPreferences

        SecurityHelper.encryptedPreferencesProvider(appComponent) shouldBe sharedPreferences
        verify { errorResetTool wasNot Called }
    }

    @Test
    fun `error case set isResetNoticeToBeShown and return null`() {
        every { preferenceFactory.create("shared_preferences_cwa") } throws Exception()

        SecurityHelper.encryptedPreferencesProvider(appComponent) shouldBe null
        isResetNoticeToBeShown.captured shouldBe true
    }
}
