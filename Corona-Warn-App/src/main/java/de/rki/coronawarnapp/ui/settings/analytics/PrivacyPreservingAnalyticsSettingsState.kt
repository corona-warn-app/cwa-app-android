package de.rki.coronawarnapp.ui.settings.analytics

import android.content.Context
import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat

data class PrivacyPreservingAnalyticsSettingsState(
    val isPpaEnabled: Boolean
) {
    /**
     * Formats the settings ppa details illustration depending on notifications status
     */
    fun getPpaImage(context: Context): Drawable? = context.getDrawableCompat(
        if (isPpaEnabled) R.drawable.ic_illustration_notification_on
        else R.drawable.ic_settings_illustration_notification_off
    )

    fun getPpaStatusText(c: Context): String = c.getString(
        if (isPpaEnabled) R.string.settings_on
        else R.string.settings_off
    )
}
