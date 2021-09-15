package de.rki.coronawarnapp.qrcode.ui

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation

class SharedQrScannerViewModel : ViewModel() {

    val verifiedTraceLocationsHashMap = HashMap<String, VerifiedTraceLocation>()
    fun getVerifiedTraceLocationByKey(key: String): VerifiedTraceLocation {
        return verifiedTraceLocationsHashMap[key]!!.also { verifiedTraceLocationsHashMap.remove(key) }
    }
}
