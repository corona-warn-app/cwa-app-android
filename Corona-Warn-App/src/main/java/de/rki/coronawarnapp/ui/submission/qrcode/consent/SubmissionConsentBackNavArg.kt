package de.rki.coronawarnapp.ui.submission.qrcode.consent

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
sealed class SubmissionConsentBackNavArg : Parcelable {
    object BackToTestRegistrationSelection : SubmissionConsentBackNavArg()
}
