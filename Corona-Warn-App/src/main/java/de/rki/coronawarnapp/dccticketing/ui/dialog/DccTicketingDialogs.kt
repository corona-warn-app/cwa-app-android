package de.rki.coronawarnapp.dccticketing.ui.dialog

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.dccTicketingConfirmCancellationDialog(negativeButtonAction: () -> Unit) = displayDialog {
    setTitle(R.string.dcc_ticketing_consent_one_cancel_dialog_title)
    setMessage(R.string.dcc_ticketing_consent_one_cancel_dialog_body)
    setPositiveButton(R.string.dcc_ticketing_consent_one_cancel_dialog_continue_btn) { _, _ -> }
    setNegativeButton(R.string.dcc_ticketing_consent_one_cancel_dialog_cancel_btn) { _, _ -> }
}

fun Fragment.dccTicketingErrorDialog(message: String) = displayDialog {
    setTitle(R.string.errors_generic_headline_short)
    setMessage(message)
    setPositiveButton(R.string.errors_generic_button_positive) { _, _ -> }
}
