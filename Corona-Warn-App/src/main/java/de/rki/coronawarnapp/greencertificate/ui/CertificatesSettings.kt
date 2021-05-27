package de.rki.coronawarnapp.greencertificate.ui

import de.rki.coronawarnapp.greencertificate.storage.CertificatesPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificatesSettings @Inject constructor(val preferences: CertificatesPreferences) {

    var onboardingStatus: OnboardingStatus
        get() {
            val order = preferences.onboardingStatusOrder.value
            return OnboardingStatus.values().find { it.order == order } ?: OnboardingStatus.NOT_ONBOARDED
        }
        set(value) = preferences.onboardingStatusOrder.update { value.order }

    inline val isOnboardingDone get() = onboardingStatus == OnboardingStatus.ONBOARDED

    enum class OnboardingStatus(val order: Int) {
        NOT_ONBOARDED(0),
        ONBOARDED(1)
    }
}
