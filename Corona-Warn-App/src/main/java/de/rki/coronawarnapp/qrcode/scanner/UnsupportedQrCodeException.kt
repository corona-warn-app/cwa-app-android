package de.rki.coronawarnapp.qrcode.scanner

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError

class UnsupportedQrCodeException(
    val errorCode: ErrorCode = ErrorCode.UNSUPPORTED_QR_CODE
) : Exception(), HasHumanReadableError {
    enum class ErrorCode {
        UNSUPPORTED_QR_CODE
    }

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            title = context.getString(R.string.qr_code_no_suitable_qr_code_title),
            description = context.getString(R.string.qr_code_no_suitable_qr_code_body)
        )
    }
}
