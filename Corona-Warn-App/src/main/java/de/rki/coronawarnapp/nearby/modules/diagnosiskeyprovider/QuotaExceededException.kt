package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError

class QuotaExceededException(
    cause: Throwable
) : IllegalStateException("Quota limit exceeded.", cause), HasHumanReadableError {

    override fun toHumanReadableError(context: Context): HumanReadableError = HumanReadableError(
        title = context.getString(R.string.errors_risk_detection_limit_reached_title),
        description = context.getString(R.string.errors_risk_detection_limit_reached_description)
    )
}
