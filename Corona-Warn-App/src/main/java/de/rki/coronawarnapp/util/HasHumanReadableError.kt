package de.rki.coronawarnapp.util

import android.content.Context

interface HasHumanReadableError {
    fun toHumanReadableError(context: Context): HumanReadableError
}

data class HumanReadableError(
    val title: String? = null,
    val description: String
)

fun Throwable.tryHumanReadableError(context: Context): HumanReadableError = when (this) {
    is HasHumanReadableError -> this.toHumanReadableError(context)
    else -> {
        HumanReadableError(
            description = (localizedMessage ?: this.message) ?: this.toString()
        )
    }
}
