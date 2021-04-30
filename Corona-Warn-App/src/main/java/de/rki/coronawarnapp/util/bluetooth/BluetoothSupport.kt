package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Build
import dagger.Reusable
import javax.inject.Inject

@Reusable
class BluetoothSupport @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter?
) {
    /**
     * Determine whether Bluetooth low Energy scanning is supported
     *
     * @return true if supported, false if not supported, null if unknown
     */
    val isScanningSupported: Boolean?
        get() = when {
            hasNoBluetooth -> false
            isBluetoothTurnedOff -> null
            hasScanner -> true
            else -> false
        }

    /**
     * Determine whether Bluetooth Low Energy peripheral mode (advertising
     * beacons) is supported
     *
     * @return true if supported, false if not supported, null if unknown
     */
    val isAdvertisingSupported: Boolean?
        get() = when {
            hasNoBluetooth -> false
            hasApi26AndSupportsAdvertising -> true
            isBluetoothTurnedOff -> null
            hasAdvertiser -> true
            else -> false
        }

    private val hasNoBluetooth: Boolean
        get() = bluetoothAdapter == null

    private val isBluetoothTurnedOff: Boolean
        get() = bluetoothAdapter?.state != BluetoothAdapter.STATE_ON

    private val hasScanner: Boolean
        get() = bluetoothAdapter?.bluetoothLeScanner != null

    private val hasAdvertiser: Boolean
        get() = bluetoothAdapter?.bluetoothLeAdvertiser != null

    private val hasApi26AndSupportsAdvertising: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            (
                bluetoothAdapter?.let {
                    it.isLeExtendedAdvertisingSupported || it.isLePeriodicAdvertisingSupported
                }
                    ?: false
                )
}
