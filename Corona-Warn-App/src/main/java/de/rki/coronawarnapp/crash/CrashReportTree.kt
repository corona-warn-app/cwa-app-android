package de.rki.coronawarnapp.crash

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CrashReportTree @Inject constructor(private val crashReportRepository: CrashReportRepository) :
    Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.ERROR) {
            GlobalScope.launch {
                crashReportRepository.createCrashReport(message, t, tag)
            }
        }
    }
}
