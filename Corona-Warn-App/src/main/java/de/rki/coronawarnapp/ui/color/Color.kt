package de.rki.coronawarnapp.ui.color

import android.graphics.Color
import androidx.annotation.ColorInt
import timber.log.Timber

/**
 * Parse color from String - default color is a fallback
 * @param defaultColor [Int] color
 */
fun String.parseColor(@ColorInt defaultColor: Int = Color.BLACK): Int =
    try {
        Color.parseColor(this)
    } catch (e: Exception) {
        Timber.d(e, "Parsing color failed")
        defaultColor
    }
