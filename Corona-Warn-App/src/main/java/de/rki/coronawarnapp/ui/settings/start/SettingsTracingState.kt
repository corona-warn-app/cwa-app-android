package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat

sealed class SettingsTracingState {

    @ColorInt
    abstract fun getTracingIconColor(context: Context): Int
    abstract fun getTracingIcon(context: Context): Drawable?
    abstract fun getTracingStatusText(context: Context): String

    object BluetoothDisabled : SettingsTracingState() {
        override fun getTracingIconColor(context: Context): Int =
            context.getColorCompat(R.color.colorTextSemanticRed)

        override fun getTracingIcon(context: Context): Drawable? =
            context.getDrawableCompat(R.drawable.ic_settings_tracing_bluetooth_inactive)

        override fun getTracingStatusText(context: Context): String =
            context.getString(R.string.settings_tracing_status_restricted)
    }

    object LocationDisabled : SettingsTracingState() {
        override fun getTracingIconColor(context: Context): Int =
            context.getColorCompat(R.color.colorTextSemanticRed)

        override fun getTracingIcon(context: Context): Drawable? =
            context.getDrawableCompat(R.drawable.ic_settings_location_inactive_small)

        override fun getTracingStatusText(context: Context): String =
            context.getString(R.string.settings_tracing_status_inactive)
    }

    object TracingActive : SettingsTracingState() {
        override fun getTracingIconColor(context: Context): Int =
            context.getColorCompat(R.color.colorAccentTintIcon)

        override fun getTracingIcon(context: Context): Drawable? =
            context.getDrawableCompat(R.drawable.ic_settings_tracing_active_small)

        override fun getTracingStatusText(context: Context): String =
            context.getString(R.string.settings_tracing_status_active)
    }

    object TracingInActive : SettingsTracingState() {
        override fun getTracingIconColor(context: Context): Int =
            context.getColorCompat(R.color.colorTextSemanticRed)

        override fun getTracingIcon(context: Context): Drawable? =
            context.getDrawableCompat(R.drawable.ic_settings_tracing_inactive_small)

        override fun getTracingStatusText(context: Context): String =
            context.getString(R.string.settings_tracing_status_inactive)
    }
}

fun GeneralTracingStatus.Status.toSettingsTracingState(): SettingsTracingState = when (this) {
    GeneralTracingStatus.Status.TRACING_ACTIVE -> SettingsTracingState.TracingActive
    GeneralTracingStatus.Status.TRACING_INACTIVE -> SettingsTracingState.TracingInActive
    GeneralTracingStatus.Status.BLUETOOTH_DISABLED -> SettingsTracingState.BluetoothDisabled
    GeneralTracingStatus.Status.LOCATION_DISABLED -> SettingsTracingState.LocationDisabled
}
