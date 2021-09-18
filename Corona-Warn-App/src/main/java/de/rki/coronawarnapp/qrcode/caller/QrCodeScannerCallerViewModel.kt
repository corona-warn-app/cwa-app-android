package de.rki.coronawarnapp.qrcode.caller

import androidx.lifecycle.ViewModel

/**
 * Shares global action from camera caller between start and end destinations
 * to have ability to navigate back to caller after camera flow complete.
 */
class QrCodeScannerCallerViewModel : ViewModel() {

    private val callerGlobalActionCache = mutableListOf<Int?>()

    fun callerGlobalAction(): Int? {
        return if (callerGlobalActionCache.size > 0) {
            val action = callerGlobalActionCache.first()
            callerGlobalActionCache.removeFirst()
            action
        } else {
            null
        }
    }

    fun putCallerGlobalAction(
        action: Int
    ) {
        callerGlobalActionCache.add(action)
    }
}
