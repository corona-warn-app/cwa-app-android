package de.rki.coronawarnapp.util

import android.content.Context
import de.rki.coronawarnapp.bugreporting.exceptions.findKnownError
import de.rki.coronawarnapp.util.ui.LazyString

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
        findKnownError(context) ?: HumanReadableError(
            description = (localizedMessage ?: this.message) ?: this.toString()
        )
    }
}

fun HasHumanReadableError.toResolvingString() = object : LazyString {
    override fun get(context: Context): String = toHumanReadableError(context).description
}
