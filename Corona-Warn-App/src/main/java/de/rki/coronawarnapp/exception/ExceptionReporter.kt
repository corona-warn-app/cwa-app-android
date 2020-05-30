package de.rki.coronawarnapp.exception

import android.util.Log
import android.widget.Toast
import de.rki.coronawarnapp.CoronaWarnApplication
import kotlinx.coroutines.runBlocking

private const val TAG: String = "ExceptionHandler"

fun Throwable.report(exceptionCategory: ExceptionCategory) =
    this.report(exceptionCategory, null, null)

fun Throwable.report(
    exceptionCategory: ExceptionCategory,
    prefix: String?,
    suffix: String?
) {
    runBlocking {
        Toast.makeText(
            CoronaWarnApplication.getAppContext(),
            this@report.localizedMessage ?: "This should never happen.",
            Toast.LENGTH_SHORT
        ).show()
    }

    Log.e(
        TAG,
        "[${exceptionCategory.name}]${(prefix ?: "")} ${(this.message ?: "Error Text Unavailable")}${(suffix ?: "")}"
    )
}
