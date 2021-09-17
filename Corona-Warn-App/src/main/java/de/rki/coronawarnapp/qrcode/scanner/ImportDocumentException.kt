package de.rki.coronawarnapp.qrcode.scanner

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError

data class ImportDocumentException(
    val errorCode: ErrorCode
) : Exception(), HasHumanReadableError {

    enum class ErrorCode {
        FILE_FORMAT_NOT_SUPPORTED,
        QR_CODE_NOT_FOUND,
        CANT_READ_FILE,
    }

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return when (errorCode) {
            ErrorCode.FILE_FORMAT_NOT_SUPPORTED -> HumanReadableError(
                title = context.getString(R.string.qr_code_file_not_readable_title),
                description = context.getString(R.string.qr_code_file_not_readable_body),
            )
            ErrorCode.QR_CODE_NOT_FOUND -> HumanReadableError(
                title = context.getString(R.string.qr_code_no_qr_code_title),
                description = context.getString(R.string.qr_code_no_qr_code_body),
            )
            ErrorCode.CANT_READ_FILE -> HumanReadableError(
                title = context.getString(R.string.qr_code_file_not_readable_title),
                description = context.getString(R.string.qr_code_file_corrupted_body),
            )
        }
    }
}
