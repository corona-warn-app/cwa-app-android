package de.rki.coronawarnapp.util.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothProvider @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val bluetoothAdapter: BluetoothAdapter?
) {

    val isBluetoothEnabled: Flow<Boolean> = callbackFlow {
        send(startingState)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED != intent.action) {
                    Timber.d("Unknown bluetooth action: %s", intent)
                    return
                }

                val value = when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                    BluetoothAdapter.STATE_OFF -> false
                    BluetoothAdapter.STATE_ON -> true
                    else -> null
                } ?: return

                this@callbackFlow.launch { send(value) }
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        awaitClose { context.unregisterReceiver(receiver) }
    }
        .onStart { Timber.v("bluetoothState FLOW start") }
        .onEach { Timber.v("bluetoothState FLOW emission: %b", it) }
        .onCompletion { Timber.v("bluetoothState FLOW completed.") }
        .stateIn(
            scope = appScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
        .mapNotNull { it }

    private val startingState: Boolean
        get() = if (bluetoothAdapter != null) {
            bluetoothAdapter.isEnabled
        } else {
            Timber.w("Device has no Bluetooth hardware")
            false
        }
}
