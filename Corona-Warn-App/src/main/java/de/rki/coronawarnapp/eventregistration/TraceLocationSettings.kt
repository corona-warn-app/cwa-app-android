package de.rki.coronawarnapp.eventregistration

import de.rki.coronawarnapp.eventregistration.storage.TraceLocationPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceLocationSettings @Inject constructor(val preferences: TraceLocationPreferences) {

    var onboardingStatus: OnboardingStatus
        get() {
            val order = preferences.onboardingStatusOrder.value
            return OnboardingStatus.values().find { it.order == order } ?: OnboardingStatus.NOT_ONBOARDED
        }
        set(value) = preferences.onboardingStatusOrder.update { value.order }

    inline val isOnboardingDone get() = onboardingStatus == OnboardingStatus.ONBOARDED_2_0

    enum class OnboardingStatus(val order: Int) {
        NOT_ONBOARDED(0),
        ONBOARDED_2_0(1)
    }
}
