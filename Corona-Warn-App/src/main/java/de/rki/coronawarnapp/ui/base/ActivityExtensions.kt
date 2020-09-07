package de.rki.coronawarnapp.ui.base

import android.app.Activity
import android.content.Intent
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.ExternalActionException
import de.rki.coronawarnapp.exception.reporting.report

fun Activity.startActivitySafely(
    intent: Intent,
    handler: (Exception) -> Unit = {
        ExternalActionException(it).report(ExceptionCategory.UI)
    }
) {
    try {
        startActivity(intent)
    } catch (exception: Exception) {
        handler.invoke(exception)
    }
}
