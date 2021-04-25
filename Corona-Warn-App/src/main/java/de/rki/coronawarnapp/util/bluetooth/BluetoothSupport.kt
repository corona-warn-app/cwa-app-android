package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Build
import dagger.Reusable
import javax.inject.Inject

@Reusable
class BluetoothSupport @Inject constructor(
    bluetoothAdapter: BluetoothAdapter?
) {
    /**
     * Determine whether Bluetooth low Energy scanning is supported
     *
     * @return true if supported, false if not supported, null if unknown
     */
    val isScanningSupported: Boolean?
        get() = when {
            !hasBluetooth -> false
            !isTurnedOn -> null
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
            !hasBluetooth -> false
            isAdvertisingSupportedApi26 -> true
            !isTurnedOn -> null
            hasAdvertiser -> true
            else -> false
        }

    private val hasBluetooth = bluetoothAdapter != null

    private val isTurnedOn = bluetoothAdapter?.state == BluetoothAdapter.STATE_ON

    private val hasScanner = bluetoothAdapter?.bluetoothLeScanner != null

    private val hasAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser != null

    private val isAdvertisingSupportedApi26 =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            (bluetoothAdapter?.let { it.isLeExtendedAdvertisingSupported || it.isLePeriodicAdvertisingSupported }
                ?: false)
}

