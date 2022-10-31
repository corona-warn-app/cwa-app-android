package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl

fun Fragment.showCovidCertificateOnboardingErrorDialog(error: Throwable) = displayDialog {
    if (error is InvalidHealthCertificateException) {
        when {
            error.isCertificateInvalid ->
                neutralButton(R.string.error_button_dcc_faq) {
                    openUrl(R.string.error_button_dcc_faq_link)
                }

            error.isSignatureInvalid -> {
                title(R.string.dcc_signature_validation_dialog_title)
                neutralButton(R.string.dcc_signature_validation_dialog_faq_button) {
                    openUrl(R.string.dcc_signature_validation_dialog_faq_link)
                }
            }
            error.isAlreadyRegistered -> {
                neutralButton(R.string.error_button_dcc_faq) {
                    openUrl(R.string.error_dcc_already_registered_faq_link)
                }
            }
        }
    }
    setError(error)
}
