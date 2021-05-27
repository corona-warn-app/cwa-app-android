package de.rki.coronawarnapp.greencertificate.ui.onboarding

sealed class CertificatesOnboardingNavigationEvents {
    object NavigateToMainActivity : CertificatesOnboardingNavigationEvents()
    object NavigateToPrivacyFragment : CertificatesOnboardingNavigationEvents()
    object NavigateToOverviewFragment : CertificatesOnboardingNavigationEvents()
}
