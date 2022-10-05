package de.rki.coronawarnapp.ui.view

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

/**
 * Returns title and subtitle alpha calculations on the current offset change
 */
fun AppBarLayout.onOffsetChange(onChange: (Float, Float) -> Unit) {
    addOnOffsetChangedListener { appBarLayout, verticalOffset ->
        val titleAlpha =
            1.0f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.5f))
        val subtitleAlpha =
            1.0f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.7f))
        onChange(titleAlpha, subtitleAlpha)
    }
}
