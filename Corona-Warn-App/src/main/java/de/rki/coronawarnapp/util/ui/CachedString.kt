package de.rki.coronawarnapp.util.ui

import android.content.Context

data class CachedString(val provider: (Context) -> String) : LazyString {
    private lateinit var cached: String

    override fun get(context: Context): String {
        if (!::cached.isInitialized) cached = provider(context)
        return cached
    }
}
