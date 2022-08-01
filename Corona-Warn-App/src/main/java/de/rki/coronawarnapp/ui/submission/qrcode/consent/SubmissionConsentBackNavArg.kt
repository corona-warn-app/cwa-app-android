package de.rki.coronawarnapp.ui.submission.qrcode.consent

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class SubmissionConsentBackNavArg : Parcelable {
    object BackToTestRegistrationSelection : SubmissionConsentBackNavArg()
}
