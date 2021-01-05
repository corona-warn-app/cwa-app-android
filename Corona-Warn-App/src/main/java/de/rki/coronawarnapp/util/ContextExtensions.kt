package de.rki.coronawarnapp.util

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

object ContextExtensions {

    fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)

    fun Context.getColorStateListCompat(@ColorRes id: Int) = ContextCompat.getColorStateList(this, id)

}
