package de.rki.coronawarnapp.covidcertificate.common.exception

import android.content.Context
import androidx.annotation.StringRes
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class TestCertificateServerException(
    val errorCode: ErrorCode
) : HasHumanReadableError, Exception(errorCode.message) {

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context)
        )
    }

    val errorMessage: LazyString
        get() = CachedString { context ->
            context.getString(errorCode.stringRes)
        }

    enum class ErrorCode(
        val message: String,
        @StringRes val stringRes: Int
    ) {
        DCC_COMP_202(
            "DCC Components request failed with error 202: DCC pending.",
            ERROR_MESSAGE_TRY_AGAIN_DCC_NOT_AVAILABLE_YET
        ),
        DCC_COMP_400(
            "DCC Components request failed with error 400: Bad request (e.g. wrong format of registration token)",
            ERROR_MESSAGE_CLIENT_ERROR_CALL_HOTLINE
        ),
        DCC_COMP_404(
            "DCC Components request failed with error 404: Registration token does not exist.",
            ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE
        ),
        DCC_COMP_410(
            "DCC Components request failed with error 410: DCC already cleaned up.",
            ERROR_MESSAGE_DCC_EXPIRED
        ),
        DCC_COMP_412(
            "DCC Components request failed with error 412: Test result not yet received",
            ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE
        ),
        DCC_COMP_500(
            "DCC Test Certificate Components failed with error 500: Internal server error.",
            ERROR_MESSAGE_TRY_AGAIN
        ),
        DCC_COMP_500_LAB_INVALID_RESPONSE(
            "DCC Components failed with error 500: Lab Invalid response",
            ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE
        ),
        DCC_COMP_500_SIGNING_CLIENT_ERROR(
            "DCC Components failed with error 500: Signing client error",
            ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE
        ),
        DCC_COMP_500_SIGNING_SERVER_ERROR(
            "DCC Components failed with error 500: Signing server error",
            ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE
        ),
        DCC_COMP_NO_NETWORK(
            "DCC Test Certificate Components failed due to no network connection.",
            ERROR_MESSAGE_NO_NETWORK
        ),
        PKR_400(
            "Public Key Registration failed with 400: " +
                "Bad request (e.g. wrong format of registration token or public key).",
            ERROR_MESSAGE_CLIENT_ERROR_CALL_HOTLINE
        ),
        PKR_403(
            "Public Key Registration failed with 403: Registration token is not allowed to issue a DCC.",
            ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE
        ),
        PKR_404(
            "Public Key Registration failed with 404: Registration token does not exist.",
            ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE
        ),
        PKR_409(
            "Public Key Registration failed with 409: Registration token is already assigned to a public key.",
            ERROR_MESSAGE_GENERIC
        ),
        PKR_500(
            "Public Key Registration failed with 500: Internal server error.",
            ERROR_MESSAGE_TRY_AGAIN
        ),
        PKR_FAILED(
            "Private key request failed.",
            ERROR_MESSAGE_TRY_AGAIN
        ),
        PKR_NO_NETWORK(
            "Private key request failed due to no network connection.",
            ERROR_MESSAGE_NO_NETWORK
        ),
    }
}
