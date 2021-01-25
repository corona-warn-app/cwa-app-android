package de.rki.coronawarnapp.contactdiary.ui

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

    enum class OnboardingStatus(val order: Int) {
        NOT_ONBOARDED(0),
        RISK_STATUS_1_12(1)
    }
}
