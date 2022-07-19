package de.rki.coronawarnapp.contactdiary.ui

import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettings
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettingsStorage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDiaryUiSettings @Inject constructor(
    private val contactDiarySettingsStorage: ContactDiarySettingsStorage
) {

    val onboardingStatus = contactDiarySettingsStorage.contactDiarySettings.map { it.onboardingStatus }

    val isOnboardingDone = onboardingStatus
        .map { it == ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12 }
        .distinctUntilChanged()

    suspend fun updateOnboardingStatus(onboardingStatus: ContactDiarySettings.OnboardingStatus) {
        contactDiarySettingsStorage.updateContactDiarySettings { it.copy(onboardingStatus = onboardingStatus) }
    }
}
