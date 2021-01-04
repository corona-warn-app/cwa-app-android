package de.rki.coronawarnapp.util.ui

import android.content.Context

interface LazyString {
    fun get(context: Context): String
}

data class CachedString(val provider: (Context) -> String) : LazyString {
    private var cached: String? = null

    override fun get(context: Context): String = cached ?: provider(context).also {
        cached = it
    }
}

fun String.toLazyString() = object : LazyString {
    override fun get(context: Context) = this@toLazyString
}

fun Int.toResolvingString(vararg formatArgs: Any): LazyString = object : LazyString {
    override fun get(context: Context): String = context.getString(this@toResolvingString, *formatArgs)
}
