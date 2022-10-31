package de.rki.coronawarnapp.tracing.ui

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.tracingConsentDialog(
    positiveButton: () -> Unit,
    negativeButton: () -> Unit
) = displayDialog {
    title(R.string.onboarding_tracing_headline_consent)
    message(R.string.onboarding_tracing_body_consent)
    positiveButton(R.string.onboarding_button_enable) { positiveButton() }
    negativeButton(R.string.onboarding_button_cancel) { negativeButton() }
}
