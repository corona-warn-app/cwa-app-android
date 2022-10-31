package de.rki.coronawarnapp.dccticketing.ui.dialog

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.dccTicketingConfirmCancellationDialog(negativeButtonAction: () -> Unit) = displayDialog {
    title(R.string.dcc_ticketing_consent_one_cancel_dialog_title)
    message(R.string.dcc_ticketing_consent_one_cancel_dialog_body)
    positiveButton(R.string.dcc_ticketing_consent_one_cancel_dialog_continue_btn)
    negativeButton(R.string.dcc_ticketing_consent_one_cancel_dialog_cancel_btn) { negativeButtonAction() }
}

fun Fragment.dccTicketingErrorDialog(message: String) = displayDialog {
    title(R.string.errors_generic_headline_short)
    message(message)
    positiveButton(R.string.errors_generic_button_positive)
}
