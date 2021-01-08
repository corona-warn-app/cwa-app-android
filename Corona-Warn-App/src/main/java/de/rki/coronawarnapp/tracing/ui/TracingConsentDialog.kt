package de.rki.coronawarnapp.tracing.ui

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.DialogHelper

class TracingConsentDialog(private val context: Context) {

    fun show(
        onConsentGiven: () -> Unit,
        onConsentDeclined: () -> Unit
    ) {
        val dialog = DialogHelper.DialogInstance(
            context = context,
            title = R.string.onboarding_tracing_headline_consent,
            message = R.string.onboarding_tracing_body_consent,
            positiveButton = R.string.onboarding_button_enable,
            negativeButton = R.string.onboarding_button_cancel,
            cancelable = true,
            positiveButtonFunction = { onConsentGiven() },
            negativeButtonFunction = { onConsentDeclined() }
        )
        DialogHelper.showDialog(dialog)
    }
}
