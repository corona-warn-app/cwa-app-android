package de.rki.coronawarnapp.tracing.ui.statusbar

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat

sealed class TracingHeaderState {
    abstract fun getTracingDescription(c: Context): String
    abstract fun getTracingContentDescription(c: Context): String
    abstract fun getTracingAnimation(c: Context): Int?
    abstract fun getTracingTint(c: Context): Int

    object BluetoothDisabled : TracingHeaderState() {

        override fun getTracingDescription(c: Context): String =
            c.getString(R.string.settings_tracing_body_bluetooth_inactive)

        override fun getTracingContentDescription(c: Context): String =
            c.getString(R.string.settings_tracing_body_bluetooth_inactive) +
                " " + c.getString(R.string.accessibility_button)

        override fun getTracingAnimation(c: Context): Int =
            R.drawable.ic_settings_tracing_bluetooth_inactive

        override fun getTracingTint(c: Context): Int = c.getColorCompat(
            R.color.colorTextSemanticRed
        )
    }

    object LocationDisabled : TracingHeaderState() {
        override fun getTracingDescription(c: Context): String =
            c.getString(R.string.settings_tracing_body_inactive_location)

        override fun getTracingContentDescription(c: Context): String =
            c.getString(R.string.settings_tracing_body_inactive_location) +
                " " + c.getString(R.string.accessibility_button)

        override fun getTracingAnimation(c: Context): Int =
            R.drawable.ic_settings_location_inactive_small

        override fun getTracingTint(c: Context): Int = c.getColorCompat(
            R.color.colorTextSemanticRed
        )
    }

    object TracingInActive : TracingHeaderState() {
        override fun getTracingDescription(c: Context): String =
            c.getString(R.string.settings_tracing_body_inactive)

        override fun getTracingContentDescription(c: Context): String =
            c.getString(R.string.settings_tracing_body_inactive) +
                " " + c.getString(R.string.accessibility_button)

        override fun getTracingAnimation(c: Context): Int =
            R.drawable.ic_settings_tracing_inactive

        override fun getTracingTint(c: Context): Int = c.getColorCompat(
            R.color.colorTextSemanticRed
        )
    }

    object TracingActive : TracingHeaderState() {
        override fun getTracingDescription(c: Context): String =
            c.getString(R.string.settings_tracing_body_active)

        override fun getTracingContentDescription(c: Context): String =
            c.getString(R.string.settings_tracing_body_active) +
                " " + c.getString(R.string.accessibility_button)

        override fun getTracingAnimation(c: Context): Int =
            R.raw.ic_settings_tracing_animated

        override fun getTracingTint(c: Context): Int = c.getColorCompat(
            R.color.colorAccentTintIcon
        )
    }
}

fun GeneralTracingStatus.Status.toHeaderState(): TracingHeaderState = when (this) {
    GeneralTracingStatus.Status.TRACING_ACTIVE -> TracingHeaderState.TracingActive
    GeneralTracingStatus.Status.TRACING_INACTIVE -> TracingHeaderState.TracingInActive
    GeneralTracingStatus.Status.BLUETOOTH_DISABLED -> TracingHeaderState.BluetoothDisabled
    GeneralTracingStatus.Status.LOCATION_DISABLED -> TracingHeaderState.LocationDisabled
}
