package de.rki.coronawarnapp.tracing.ui

import android.content.Context
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.DialogHelper

class TracingConsentDialog(private val context: Context) {

    // About to be refactored, use extension function below
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

fun Fragment.tracingConsentDialog(
    positiveButton: () -> Unit,
    negativeButton: () -> Unit
) = displayDialog {
    setTitle(R.string.onboarding_tracing_headline_consent)
    setMessage(R.string.onboarding_tracing_body_consent)
    setPositiveButton(R.string.onboarding_button_enable) { _, _ -> positiveButton() }
    setNegativeButton(R.string.onboarding_button_cancel) { _, _ -> negativeButton() }
}
