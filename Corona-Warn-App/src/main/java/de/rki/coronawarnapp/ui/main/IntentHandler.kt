package de.rki.coronawarnapp.ui.main

import android.content.Intent
import android.net.Uri
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.ui.submission.qrcode.consent.SubmissionConsentFragment
import timber.log.Timber
import javax.inject.Inject

class IntentHandler @Inject constructor(
    private val qrCodeValidator: CoronaTestQrCodeValidator
) {

    fun generateDeepLink(intent: Intent?): Uri? {
        val uriString = intent?.data?.toString() ?: return null
        Timber.i("Uri:$uriString")
        return when {
            CheckInsFragment.canHandle(uriString) -> CheckInsFragment.createDeepLink(uriString)
            qrCodeValidator.canHandle(uriString) -> SubmissionConsentFragment.createDeepLink(uriString)
            else -> null
        }
    }
}

