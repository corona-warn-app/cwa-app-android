package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.FormattedError

class QuotaExceededException(
    cause: Throwable
) : IllegalStateException(
    "Quota limit exceeded.",
    cause
), FormattedError {

    override fun getFormattedError(context: Context): FormattedError.Info = FormattedError.Info(
        message = context.getString(R.string.errors_risk_detection_limit_reached)
    )
}
