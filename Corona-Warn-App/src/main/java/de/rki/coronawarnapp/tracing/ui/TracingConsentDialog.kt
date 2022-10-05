package de.rki.coronawarnapp.tracing.ui

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.tracingConsentDialog(
    positiveButton: () -> Unit,
    negativeButton: () -> Unit
) = displayDialog {
    setTitle(R.string.onboarding_tracing_headline_consent)
    setMessage(R.string.onboarding_tracing_body_consent)
    setPositiveButton(R.string.onboarding_button_enable) { _, _ -> positiveButton() }
    setNegativeButton(R.string.onboarding_button_cancel) { _, _ -> negativeButton() }
}
