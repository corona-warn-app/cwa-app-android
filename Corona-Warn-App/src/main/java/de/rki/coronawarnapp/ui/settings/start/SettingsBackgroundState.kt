package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat

data class SettingsBackgroundState(
    val isEnabled: Boolean
) {

    /**
     * Formats the settings icon color for background priority
     */
    @ColorInt
    fun getBackgroundPriorityIconColor(c: Context): Int = c.getColorCompat(
        if (isEnabled) R.color.colorAccentTintIcon
        else R.color.colorTextSemanticRed
    )

    /**
     * Formats the settings icon for background priority
     */
    fun getBackgroundPriorityIcon(context: Context): Drawable? = context.getDrawableCompat(
        if (isEnabled) R.drawable.ic_settings_background_priority_enabled
        else R.drawable.ic_settings_background_priority_disabled
    )

    /**
     * Formats the text display of settings item status depending on flag provided
     */
    fun getBackgroundPriorityText(c: Context): String = c.getString(
        if (isEnabled) R.string.settings_on else R.string.settings_off
    )
}
