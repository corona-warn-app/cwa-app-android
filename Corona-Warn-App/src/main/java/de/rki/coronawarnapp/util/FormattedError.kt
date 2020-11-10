package de.rki.coronawarnapp.util

import android.content.Context

interface FormattedError {
    fun getFormattedError(context: Context): Info

    data class Info(
        val message: String
    )
}

fun Throwable.tryFormattedError(context: Context): FormattedError.Info = when (this) {
    is FormattedError -> this.getFormattedError(context)
    else -> {
        FormattedError.Info(
            message = (localizedMessage ?: this.message) ?: this.toString()
        )
    }
}
