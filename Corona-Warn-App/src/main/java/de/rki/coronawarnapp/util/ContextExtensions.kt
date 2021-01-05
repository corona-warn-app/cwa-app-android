package de.rki.coronawarnapp.util

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * These context extensions provide easier access to the ContextCompat calls that we want
 * to call.
 *
 * This is wanted because it increases compatibility with Android versions < API level 23
 * (from which downstream projects may benefit).
 */
object ContextExtensions {

    fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)

    fun Context.getColorStateListCompat(@ColorRes id: Int) = ContextCompat.getColorStateList(this, id)

}
