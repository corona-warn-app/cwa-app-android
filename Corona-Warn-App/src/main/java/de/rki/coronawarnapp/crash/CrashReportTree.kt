package de.rki.coronawarnapp.crash

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class CrashReportTree(private val crashReportRepository: CrashReportRepository) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.ERROR) {
            CoroutineScope(Dispatchers.IO).launch {
                crashReportRepository.createCrashReport(message, t, tag)
            }
        }
    }
}
