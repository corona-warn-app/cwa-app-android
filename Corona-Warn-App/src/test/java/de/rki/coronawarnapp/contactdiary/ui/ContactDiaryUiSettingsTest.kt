package de.rki.coronawarnapp.contactdiary.ui

import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettings
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettingsStorage
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ContactDiaryUiSettingsTest : BaseTest() {

    @RelaxedMockK lateinit var contactDiarySettingsStorage: ContactDiarySettingsStorage

    private val instance: ContactDiaryUiSettings
        get() = ContactDiaryUiSettings(contactDiarySettingsStorage)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `not onboarded`() = runTest {
        every { contactDiarySettingsStorage.contactDiarySettings } returns createContactDiarySettingsFlow(
            onboardingStatus = ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED
        )

        with(instance) {
            onboardingStatus.first() shouldBe ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED
            isOnboardingDone.first() shouldBe false
        }
    }

    @Test
    fun `is onboarded`() = runTest {
        every { contactDiarySettingsStorage.contactDiarySettings } returns createContactDiarySettingsFlow(
            onboardingStatus = ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12
        )

        with(instance) {
            onboardingStatus.first() shouldBe ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12
            isOnboardingDone.first() shouldBe true
        }
    }

    @Test
    fun `updates storage`() = runTest {
        instance.updateOnboardingStatus(onboardingStatus = ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12)

        coVerify {
            contactDiarySettingsStorage.updateContactDiarySettings(any())
        }
    }

    private fun createContactDiarySettingsFlow(
        onboardingStatus: ContactDiarySettings.OnboardingStatus
    ) = flowOf(ContactDiarySettings(onboardingStatus = onboardingStatus))
}
