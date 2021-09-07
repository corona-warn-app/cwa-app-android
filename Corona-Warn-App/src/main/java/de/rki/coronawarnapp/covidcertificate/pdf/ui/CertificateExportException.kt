package de.rki.coronawarnapp.covidcertificate.pdf.ui

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError

open class CertificateExportException(
    cause: Throwable?,
    message: String?
) : Exception(message, cause), HasHumanReadableError {

    override fun toHumanReadableError(context: Context): HumanReadableError = HumanReadableError(
        description = context.getString(R.string.pdf_export_error_message),
        title = context.getString(R.string.pdf_export_error_title)
    )
}
