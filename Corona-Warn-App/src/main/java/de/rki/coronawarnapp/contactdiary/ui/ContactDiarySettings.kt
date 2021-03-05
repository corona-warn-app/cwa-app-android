package de.rki.coronawarnapp.contactdiary.ui

import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDiarySettings @Inject constructor(val preferences: ContactDiaryPreferences) {

    var onboardingStatus: OnboardingStatus
        get() {
            val order = preferences.onboardingStatusOrder.value
            return OnboardingStatus.values().find { it.order == order } ?: OnboardingStatus.NOT_ONBOARDED
        }
        set(value) = preferences.onboardingStatusOrder.update { value.order }

    inline val isOnboardingDone get() = onboardingStatus == OnboardingStatus.RISK_STATUS_1_12

    enum class OnboardingStatus(val order: Int) {
        NOT_ONBOARDED(0),
        RISK_STATUS_1_12(1)
    }
}
