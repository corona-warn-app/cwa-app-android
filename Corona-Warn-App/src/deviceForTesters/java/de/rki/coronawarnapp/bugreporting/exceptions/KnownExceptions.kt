package de.rki.coronawarnapp.bugreporting.exceptions

import android.content.Context
import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HumanReadableError

interface KnownException {
    fun matches(throwable: Throwable): Boolean

    fun getHRE(context: Context): HumanReadableError
}

enum class KnownExceptions : KnownException {
    // ApiException with StatusCode 17
    ENS_NOT_INSTALLED {
        override fun matches(throwable: Throwable): Boolean = throwable is ApiException && throwable.statusCode == 17

        override fun getHRE(context: Context): HumanReadableError = HumanReadableError(
            title = "${context.getString(R.string.errors_generic_details_headline)}: 3\n" +
                context.getString(R.string.errors_generic_headline),
            description = context.getString(R.string.errors_google_update_needed)
        )
    },
}

fun Throwable.findKnownError(context: Context): HumanReadableError? {
    return KnownExceptions.values().find { it.matches(this@findKnownError) }?.getHRE(context)
}
