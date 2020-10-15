package de.rki.coronawarnapp.util.ui

import android.content.Context

interface LazyString {
    fun get(context: Context): String
}
