package de.rki.coronawarnapp.qrcode.ui

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.ui.toResolvingString
import java.lang.IllegalArgumentException

fun Throwable.toQrCodeErrorDialogBuilder(context: Context): MaterialAlertDialogBuilder {
    val throwable = this
    return toErrorDialogBuilder(context).apply {
        if (throwable is InvalidHealthCertificateException) {
            when {
                throwable.isCertificateInvalid ->
                    setNeutralButton(R.string.error_button_dcc_faq) { _, _ ->
                        context.openUrl(R.string.error_button_dcc_faq_link)
                    }

                throwable.isSignatureInvalid -> {
                    setTitle(R.string.dcc_signature_validation_dialog_title)
                    setNeutralButton(R.string.dcc_signature_validation_dialog_faq_button) { _, _ ->
                        context.openUrl(R.string.dcc_signature_validation_dialog_faq_link)
                    }
                }
                throwable.isAlreadyRegistered -> {
                    setNeutralButton(R.string.error_button_dcc_faq) { _, _ ->
                        context.openUrl(R.string.error_dcc_already_registered_faq_link)
                    }
                }
            }
        }
    }
}

fun CertificateContainerId.toDccResult(): DccResult = when (this) {
    is RecoveryCertificateContainerId -> DccResult.Recovery(this)
    is TestCertificateContainerId -> DccResult.Test(this)
    is VaccinationCertificateContainerId -> DccResult.Vaccination(this)
}

fun CheckInQrCodeHandler.Result.toCheckInResult(): CheckInResult = when (this) {
    is CheckInQrCodeHandler.Result.Invalid -> CheckInResult.Error(errorTextRes.toResolvingString())
    is CheckInQrCodeHandler.Result.Valid -> CheckInResult.Details(verifiedTraceLocation)
}
