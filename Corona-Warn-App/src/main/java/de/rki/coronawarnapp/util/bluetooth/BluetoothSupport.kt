package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Build
import dagger.Reusable
import de.rki.coronawarnapp.util.ApiLevel
import javax.inject.Inject

@Reusable
class BluetoothSupport @Inject constructor(
    private val apiLevel: ApiLevel,
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val adapter = bluetoothAdapter
            return when {
                adapter == null -> false
                apiLevel.hasAPILevel(Build.VERSION_CODES.O)
                    && (adapter.isLeExtendedAdvertisingSupported || adapter.isLePeriodicAdvertisingSupported) -> true
                adapter.state != BluetoothAdapter.STATE_ON -> null
                adapter.bluetoothLeAdvertiser != null -> true
                else -> false
            }
        }
        return false
    }
}
