package de.rki.coronawarnapp.contactdiary.storage.settings

import com.fasterxml.jackson.annotation.JsonProperty

data class ContactDiarySettings(
    @JsonProperty("onboardingStatus") val onboardingStatus: OnboardingStatus = OnboardingStatus.NOT_ONBOARDED
) {
    enum class OnboardingStatus(val order: Int) {
        @JsonProperty("NOT_ONBOARDED")
        NOT_ONBOARDED(0),

        @JsonProperty("RISK_STATUS_1_12")
        RISK_STATUS_1_12(1)
    }
}
