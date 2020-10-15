package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CWABluetooth @Inject constructor(
    @AppContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {

    val isBluetoothEnabled: Flow<Boolean>
        get() = bluetoothState

    private val bluetoothState by lazy {
        MutableStateFlow(startingState)
            .apply {
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        if (BluetoothAdapter.ACTION_STATE_CHANGED != intent.action) {
                            Timber.d("Unknown bluetooth action: %s", intent)
                            return
                        }

                        when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                            BluetoothAdapter.STATE_OFF -> value = false
                            BluetoothAdapter.STATE_ON -> value = true
                        }
                    }
                }
                context.registerReceiver(
                    receiver,
                    IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                )
            }
            .onStart { Timber.v("bluetoothState FLOW start") }
            .onEach { Timber.v("bluetoothState FLOW emission: %b", it) }
            .onCompletion { Timber.v("bluetoothState FLOW completed.") }
    }

    private val startingState: Boolean
        get() = if (bluetoothAdapter != null) {
            bluetoothAdapter.isEnabled
        } else {
            Timber.w("Device has no Bluetooth hardware")
            false
        }
}
