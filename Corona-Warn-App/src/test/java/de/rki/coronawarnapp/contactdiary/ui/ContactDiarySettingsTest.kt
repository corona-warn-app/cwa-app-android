package de.rki.coronawarnapp.contactdiary.ui

import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryPreferences
import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContactDiarySettingsTest {

    @MockK lateinit var preferences: ContactDiaryPreferences
    @MockK lateinit var intPreference: FlowPreference<Int>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { preferences.onboardingStatusOrder } returns intPreference
    }

    @Test
    fun `not onboarded`() {
        every { intPreference.value } returns 0
        ContactDiarySettings(preferences).onboardingStatus shouldBe ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED
    }

    @Test
    fun `bad preference values`() {
        every { intPreference.value } returns 42
        ContactDiarySettings(preferences).onboardingStatus shouldBe ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED

        every { intPreference.value } returns -42
        ContactDiarySettings(preferences).onboardingStatus shouldBe ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED

        every { intPreference.value } returns Int.MAX_VALUE
        ContactDiarySettings(preferences).onboardingStatus shouldBe ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED

        every { intPreference.value } returns Int.MIN_VALUE
        ContactDiarySettings(preferences).onboardingStatus shouldBe ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED
    }

    @Test
    fun onboarded() {
        every { intPreference.value } returns 1
        ContactDiarySettings(preferences).onboardingStatus shouldBe
            ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12
    }
}
