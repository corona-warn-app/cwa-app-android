package de.rki.coronawarnapp.util

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

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

    fun Context.getDrawableCompat(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)
}

@Throws(Resources.NotFoundException::class)
fun Resources.getDrawableCompat(@DrawableRes id: Int, theme: Resources.Theme? = null) =
    ResourcesCompat.getDrawable(this, id, theme)
