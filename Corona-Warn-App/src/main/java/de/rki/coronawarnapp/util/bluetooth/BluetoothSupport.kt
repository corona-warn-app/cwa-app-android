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
    fun isScanningSupported(): Boolean? {
        val adapter = bluetoothAdapter
        return when {
            adapter == null -> false
            adapter.state != BluetoothAdapter.STATE_ON -> null
            adapter.bluetoothLeScanner != null -> true
            else -> false
        }
    }

    /**
     * Determine whether Bluetooth Low Energy peripheral mode (advertising
     * beacons) is supported
     *
     * @return true if supported, false if not supported, null if unknown
     */
    fun isAdvertisingSupported(): Boolean? {
        val adapter = bluetoothAdapter
        return when {
            adapter == null -> false
            Build.VERSION.SDK_INT >= 26
                && (adapter.isLeExtendedAdvertisingSupported || adapter.isLePeriodicAdvertisingSupported) -> true
            adapter.state != BluetoothAdapter.STATE_ON -> null
            adapter.bluetoothLeAdvertiser != null -> true
            else -> false
        }
    }
}
