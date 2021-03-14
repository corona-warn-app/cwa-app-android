package de.rki.coronawarnapp.contactdiary.ui.onboarding

sealed class ContactDiaryOnboardingNavigationEvents {
    object NavigateToMainActivity : ContactDiaryOnboardingNavigationEvents()
    object NavigateToPrivacyFragment : ContactDiaryOnboardingNavigationEvents()
    object NavigateToOverviewFragment : ContactDiaryOnboardingNavigationEvents()
}
