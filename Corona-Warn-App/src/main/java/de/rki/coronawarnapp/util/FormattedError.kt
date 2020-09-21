package de.rki.coronawarnapp.util

import android.content.Context

interface FormattedError {
    fun getFormattedError(context: Context): String
}

fun Throwable.tryFormattedError(context: Context): String = when {
    this is FormattedError -> this.getFormattedError(context)
    localizedMessage != null -> localizedMessage!!
    else -> this.toString()
}
