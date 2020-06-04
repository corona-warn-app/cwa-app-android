package de.rki.coronawarnapp.exception

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.rki.coronawarnapp.CoronaWarnApplication

fun Throwable.report(exceptionCategory: ExceptionCategory) =
    this.report(exceptionCategory, null, null)

fun Throwable.report(
    exceptionCategory: ExceptionCategory,
    prefix: String?,
    suffix: String?
) {
    val intent = Intent("error-report")
    intent.putExtra("category", exceptionCategory.name)
    intent.putExtra("prefix", prefix)
    intent.putExtra("suffix", suffix)
    intent.putExtra("message", this.message)
    LocalBroadcastManager.getInstance(CoronaWarnApplication.getAppContext()).sendBroadcast(intent)
}
