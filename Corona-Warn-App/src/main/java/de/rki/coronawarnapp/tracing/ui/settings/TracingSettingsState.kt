package de.rki.coronawarnapp.tracing.ui.settings

import android.content.Context
import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat

sealed class TracingSettingsState {

    /**
     * Formats the settings tracing details illustration depending on tracing status
     */
    abstract fun getTracingStatusImage(context: Context): Drawable?

    /**
     * Format the settings tracing content description for the header illustration
     */
    abstract fun getTracingIllustrationText(context: Context): String

    /**
     * Formats the tracing switch enabled status based on the tracing status
     */
    abstract fun isTracingSwitchEnabled(): Boolean

    /**
     * Formats the tracing switch status based on the tracing status
     */
    abstract fun isTracingSwitchChecked(): Boolean

    /**
     * Change the tracing text in the row based on the tracing status.
     */
    abstract fun getTracingStatusText(context: Context): String

    /**
     * Change the visibility of the location card based on the tracing status.
     */
    abstract fun isLocationCardVisible(): Boolean

    /**
     * Change the visibility of the bluetooth card based on the tracing status.
     */
    abstract fun isBluetoothCardVisible(): Boolean

    /**
     * Change the visibility of the tracing text based on the tracing status.
     */
    abstract fun isTracingStatusTextVisible(): Boolean

    object BluetoothDisabled : TracingSettingsState() {
        override fun getTracingStatusImage(context: Context): Drawable? =
            context.getDrawableCompat(R.drawable.ic_settings_illustration_bluetooth_off)

        override fun getTracingIllustrationText(context: Context): String =
            context.getString(R.string.settings_tracing_bluetooth_illustration_description_inactive)

        override fun isTracingSwitchEnabled(): Boolean = false

        override fun isTracingSwitchChecked(): Boolean = false

        override fun getTracingStatusText(context: Context): String =
            context.getString(R.string.settings_tracing_status_restricted)

        override fun isLocationCardVisible(): Boolean = false

        override fun isBluetoothCardVisible(): Boolean = true

        override fun isTracingStatusTextVisible(): Boolean = false
    }

    object LocationDisabled : TracingSettingsState() {
        override fun getTracingStatusImage(context: Context): Drawable? =
            context.getDrawableCompat(R.drawable.ic_settings_illustration_location_off)

        override fun getTracingIllustrationText(context: Context): String =
            context.getString(R.string.settings_tracing_location_illustration_description_inactive)

        override fun isTracingSwitchEnabled(): Boolean = false

        override fun isTracingSwitchChecked(): Boolean = false

        override fun getTracingStatusText(context: Context): String =
            context.getString(R.string.settings_tracing_status_inactive)

        override fun isLocationCardVisible(): Boolean = true

        override fun isBluetoothCardVisible(): Boolean = false

        override fun isTracingStatusTextVisible(): Boolean = false
    }

    object TracingInactive : TracingSettingsState() {
        override fun getTracingStatusImage(context: Context): Drawable? =
            context.getDrawableCompat(R.drawable.ic_settings_illustration_tracing_off)

        override fun getTracingIllustrationText(context: Context): String =
            context.getString(R.string.settings_tracing_illustration_description_inactive)

        override fun isTracingSwitchEnabled(): Boolean = true

        override fun isTracingSwitchChecked(): Boolean = false

        override fun getTracingStatusText(context: Context): String =
            context.getString(R.string.settings_tracing_status_inactive)

        override fun isLocationCardVisible(): Boolean = false

        override fun isBluetoothCardVisible(): Boolean = false

        override fun isTracingStatusTextVisible(): Boolean = true
    }

    object TracingActive : TracingSettingsState() {
        override fun getTracingStatusImage(context: Context): Drawable? =
            context.getDrawableCompat(R.drawable.ic_illustration_tracing_on)

        override fun getTracingIllustrationText(context: Context): String =
            context.getString(R.string.settings_tracing_illustration_description_active)

        override fun isTracingSwitchEnabled(): Boolean = true

        override fun isTracingSwitchChecked(): Boolean = true

        override fun getTracingStatusText(context: Context): String =
            context.getString(R.string.settings_tracing_status_active)

        override fun isLocationCardVisible(): Boolean = false

        override fun isBluetoothCardVisible(): Boolean = false

        override fun isTracingStatusTextVisible(): Boolean = true
    }
}

fun GeneralTracingStatus.Status.toTracingSettingsState(): TracingSettingsState = when (this) {
    GeneralTracingStatus.Status.TRACING_ACTIVE -> TracingSettingsState.TracingActive
    GeneralTracingStatus.Status.TRACING_INACTIVE -> TracingSettingsState.TracingInactive
    GeneralTracingStatus.Status.BLUETOOTH_DISABLED -> TracingSettingsState.BluetoothDisabled
    GeneralTracingStatus.Status.LOCATION_DISABLED -> TracingSettingsState.LocationDisabled
}
