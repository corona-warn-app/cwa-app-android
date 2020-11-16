package de.rki.coronawarnapp.ui.submission

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.submission.SubmissionSettings
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SubmissionSettingsTest {

    private val appContext: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun consentIsPersisted() {
        val settings = SubmissionSettings(appContext)
        settings.hasGivenConsent.value shouldBe false
        settings.hasGivenConsent.update { true }
        settings.hasGivenConsent.value shouldBe false
    }
}
