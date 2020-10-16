package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.tracing.TracingStatus

sealed class SettingsTracingState {

    @ColorInt
    abstract fun getTracingIconColor(c: Context): Int
    abstract fun getTracingIcon(c: Context): Drawable?
    abstract fun getTracingStatusText(c: Context): String

    object BluetoothDisabled : SettingsTracingState() {
        override fun getTracingIconColor(c: Context): Int =
            c.getColor(R.color.colorTextPrimary3)

        // TODO Why are we using the active indicator for Bluetooth?
        override fun getTracingIcon(c: Context): Drawable? =
            c.getDrawable(R.drawable.ic_settings_tracing_active_small)

        override fun getTracingStatusText(c: Context): String =
            c.getString(R.string.settings_tracing_status_restricted)
    }

    object LocationDisabled : SettingsTracingState() {
        override fun getTracingIconColor(c: Context): Int =
            c.getColor(R.color.colorTextSemanticRed)

        override fun getTracingIcon(c: Context): Drawable? =
            c.getDrawable(R.drawable.ic_settings_location_inactive_small)

        override fun getTracingStatusText(c: Context): String =
            c.getString(R.string.settings_tracing_status_inactive)
    }

    object TracingActive : SettingsTracingState() {
        override fun getTracingIconColor(c: Context): Int =
            c.getColor(R.color.colorAccentTintIcon)

        override fun getTracingIcon(c: Context): Drawable? =
            c.getDrawable(R.drawable.ic_settings_tracing_active_small)

        override fun getTracingStatusText(c: Context): String =
            c.getString(R.string.settings_tracing_status_active)
    }

    object TracingInActive : SettingsTracingState() {
        override fun getTracingIconColor(c: Context): Int =
            c.getColor(R.color.colorTextSemanticRed)

        override fun getTracingIcon(c: Context): Drawable? =
            c.getDrawable(R.drawable.ic_settings_tracing_inactive_small)

        override fun getTracingStatusText(c: Context): String =
            c.getString(R.string.settings_tracing_status_inactive)
    }
}

fun TracingStatus.Status.toSettingsTracingState(): SettingsTracingState = when (this) {
    TracingStatus.Status.TRACING_ACTIVE -> SettingsTracingState.TracingActive
    TracingStatus.Status.TRACING_INACTIVE -> SettingsTracingState.TracingInActive
    TracingStatus.Status.BLUETOOTH_DISABLED -> SettingsTracingState.BluetoothDisabled
    TracingStatus.Status.LOCATION_DISABLED -> SettingsTracingState.LocationDisabled
}
