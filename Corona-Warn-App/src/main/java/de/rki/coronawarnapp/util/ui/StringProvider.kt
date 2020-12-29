package de.rki.coronawarnapp.util.ui

import android.content.Context
import androidx.annotation.StringRes

interface StringProvider {
    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String
}

class ContextStringProvider(private val context: Context): StringProvider {
    override fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return context.resources.getString(resId, *formatArgs)
    }
}
