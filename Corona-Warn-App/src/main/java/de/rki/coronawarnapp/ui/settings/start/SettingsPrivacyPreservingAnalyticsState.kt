package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat

data class SettingsPrivacyPreservingAnalyticsState(
    val isEnabled: Boolean
) {

    /**
     * Formats the settings icon color for privacy-preserving analytics
     */
    @ColorInt
    fun getPrivacyPreservingAnalyticsIconColor(context: Context): Int = context.getColorCompat(
        if (isEnabled) R.color.colorAccentTintIcon
        else R.color.colorTextSemanticRed
    )

    /**
     * Formats the settings icon for privacy-preserving analytics
     */
    fun getPrivacyPreservingAnalyticsIcon(context: Context): Drawable? = context.getDrawableCompat(
        if (isEnabled) R.drawable.ic_settings_privacy_preserving_analytics_enabled
        else R.drawable.ic_settings_privacy_preserving_analytics_disabled
    )

    /**
     * Formats the text display of settings item status depending on flag provided
     */
    fun getPrivacyPreservingAnalyticsText(context: Context): String = context.getString(
        if (isEnabled) R.string.settings_on else R.string.settings_off
    )
}
