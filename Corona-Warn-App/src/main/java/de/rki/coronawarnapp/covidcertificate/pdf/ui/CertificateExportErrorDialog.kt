package de.rki.coronawarnapp.covidcertificate.pdf.ui

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R

object CertificateExportErrorDialog {

    /**
     * Show error dialog if PDF could not be exported in all certificate details fragments
     * @see RecoveryCertificateDetailsFragment
     * @see VaccinationDetailsFragment
     * @see TestCertificateDetailsFragment
     */
    fun showDialog(
        context: Context,
        openUrl: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.certificate_export_error_dialog_title)
            setMessage(R.string.certificate_export_error_dialog_body)
            setNegativeButton(R.string.certificate_export_error_dialog_faq_button) { _, _ ->
                openUrl()
            }
            setPositiveButton(R.string.certificate_export_error_dialog_ok_button) { _, _ -> }
        }.show()
    }
}
