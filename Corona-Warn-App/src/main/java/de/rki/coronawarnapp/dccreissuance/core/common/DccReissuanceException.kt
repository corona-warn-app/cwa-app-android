package de.rki.coronawarnapp.dccreissuance.core.common

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toResolvingString

class DccReissuanceException(
    val errorCode: ErrorCode,
    cause: Throwable? = null
) : Exception(errorCode.message, cause), HasHumanReadableError {

    //TODO: Add error codes
    enum class ErrorCode(val message: String) {

    }

    //TODO: Added error messages
    val errorMessage: LazyString
        get() = when (errorCode) {
            else -> R.string.errors_generic_text_unknown_error_cause
        }.toResolvingString()

    override fun toHumanReadableError(context: Context): HumanReadableError = HumanReadableError(
        description = "${errorMessage.get(context)} ($errorCode)"
    )
}
