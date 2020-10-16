package de.rki.coronawarnapp.ui.tracing.settings

import android.content.Context
import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.tracing.GeneralTracingStatus

sealed class TracingSettingsState {

    /**
     * Formats the settings tracing details illustration depending on tracing status
     */
    abstract fun getTracingStatusImage(c: Context): Drawable?

    /**
     * Format the settings tracing content description for the header illustration
     */
    abstract fun getTracingIllustrationText(c: Context): String

    /**
     * Formats the tracing switch enabled status based on the tracing status
     */
    abstract fun isTracingSwitchEnabled(c: Context): Boolean

    /**
     * Formats the tracing switch status based on the tracing status
     */
    abstract fun isTracingSwitchChecked(c: Context): Boolean

    /**
     * Change the tracing text in the row based on the tracing status.
     */
    abstract fun getTracingStatusText(c: Context): String

    /**
     * Change the visibility of the location card based on the tracing status.
     */
    abstract fun isLocationCardVisible(c: Context): Boolean

    /**
     * Change the visibility of the bluetooth card based on the tracing status.
     */
    abstract fun isBluetoothCardVisible(c: Context): Boolean

    /**
     * Change the visibility of the tracing text based on the tracing status.
     */
    abstract fun isTracingStatusTextVisible(c: Context): Boolean

    object BluetoothDisabled : TracingSettingsState() {
        override fun getTracingStatusImage(c: Context): Drawable? =
            c.getDrawable(R.drawable.ic_settings_illustration_bluetooth_off)

        override fun getTracingIllustrationText(c: Context): String =
            c.getString(R.string.settings_tracing_bluetooth_illustration_description_inactive)

        override fun isTracingSwitchEnabled(c: Context): Boolean = false

        override fun isTracingSwitchChecked(c: Context): Boolean = false

        override fun getTracingStatusText(c: Context): String =
            c.getString(R.string.settings_tracing_status_restricted)

        override fun isLocationCardVisible(c: Context): Boolean = false

        override fun isBluetoothCardVisible(c: Context): Boolean = true

        override fun isTracingStatusTextVisible(c: Context): Boolean = false
    }

    object LocationDisabled : TracingSettingsState() {
        override fun getTracingStatusImage(c: Context): Drawable? =
            c.getDrawable(R.drawable.ic_settings_illustration_location_off)

        override fun getTracingIllustrationText(c: Context): String =
            c.getString(R.string.settings_tracing_location_illustration_description_inactive)

        override fun isTracingSwitchEnabled(c: Context): Boolean = false

        override fun isTracingSwitchChecked(c: Context): Boolean = false

        override fun getTracingStatusText(c: Context): String =
            c.getString(R.string.settings_tracing_status_inactive)

        override fun isLocationCardVisible(c: Context): Boolean = true

        override fun isBluetoothCardVisible(c: Context): Boolean = false

        override fun isTracingStatusTextVisible(c: Context): Boolean = false
    }

    object TracingInActive : TracingSettingsState() {
        override fun getTracingStatusImage(c: Context): Drawable? =
            c.getDrawable(R.drawable.ic_settings_illustration_tracing_off)

        override fun getTracingIllustrationText(c: Context): String =
            c.getString(R.string.settings_tracing_illustration_description_inactive)

        override fun isTracingSwitchEnabled(c: Context): Boolean = true

        override fun isTracingSwitchChecked(c: Context): Boolean = false

        override fun getTracingStatusText(c: Context): String =
            c.getString(R.string.settings_tracing_status_inactive)

        override fun isLocationCardVisible(c: Context): Boolean = false

        override fun isBluetoothCardVisible(c: Context): Boolean = false

        override fun isTracingStatusTextVisible(c: Context): Boolean = true
    }

    object TracingActive : TracingSettingsState() {
        override fun getTracingStatusImage(c: Context): Drawable? =
            c.getDrawable(R.drawable.ic_illustration_tracing_on)

        override fun getTracingIllustrationText(c: Context): String =
            c.getString(R.string.settings_tracing_illustration_description_active)

        override fun isTracingSwitchEnabled(c: Context): Boolean = true

        override fun isTracingSwitchChecked(c: Context): Boolean = true

        override fun getTracingStatusText(c: Context): String =
            c.getString(R.string.settings_tracing_status_active)

        override fun isLocationCardVisible(c: Context): Boolean = false

        override fun isBluetoothCardVisible(c: Context): Boolean = false

        override fun isTracingStatusTextVisible(c: Context): Boolean = true
    }
}

fun GeneralTracingStatus.Status.toTracingSettingsState(): TracingSettingsState = when (this) {
    GeneralTracingStatus.Status.TRACING_ACTIVE -> TracingSettingsState.TracingActive
    GeneralTracingStatus.Status.TRACING_INACTIVE -> TracingSettingsState.TracingInActive
    GeneralTracingStatus.Status.BLUETOOTH_DISABLED -> TracingSettingsState.BluetoothDisabled
    GeneralTracingStatus.Status.LOCATION_DISABLED -> TracingSettingsState.LocationDisabled
}
