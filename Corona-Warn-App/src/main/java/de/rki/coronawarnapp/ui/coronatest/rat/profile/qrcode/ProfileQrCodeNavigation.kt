package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

sealed class ProfileQrCodeNavigation {
    object Back : ProfileQrCodeNavigation()
    object SubmissionConsent : ProfileQrCodeNavigation()
}
