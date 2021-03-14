package de.rki.coronawarnapp.ui.settings.backgroundpriority

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import de.rki.coronawarnapp.R

data class BackgroundPriorityState(
    val isBackgroundPriorityEnabled: Boolean
) {
    fun getButtonStateLabel(context: Context): String = context.getString(
        if (isBackgroundPriorityEnabled) R.string.settings_on
        else R.string.settings_off
    )

    fun getHeaderIllustration(context: Context): Drawable? {
        val illustrationId = if (isBackgroundPriorityEnabled) {
            R.drawable.ic_settings_illustration_background_priority_enabled
        } else {
            R.drawable.ic_settings_illustration_background_priority_disabled
        }
        return ContextCompat.getDrawable(context, illustrationId)
    }

    fun getHeaderIllustrationDescription(context: Context): String {
        val illustrationDescription = if (isBackgroundPriorityEnabled) {
            R.string.settings_background_priority_on_illustration_description
        } else {
            R.string.settings_background_priority_off_illustration_description
        }
        return context.getString(illustrationDescription)
    }
}
