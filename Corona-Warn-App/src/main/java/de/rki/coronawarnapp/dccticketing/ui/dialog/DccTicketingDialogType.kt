package de.rki.coronawarnapp.dccticketing.ui.dialog

import de.rki.coronawarnapp.R

sealed class DccTicketingDialogType {
    abstract val config: DccTicketingDialogFragment.Config

    object ConfirmCancelation : DccTicketingDialogType() {
        override val config: DccTicketingDialogFragment.Config
            get() = DccTicketingDialogFragment.Config(
                titleRes = R.string.dcc_ticketing_consent_one_cancel_dialog_title,
                msgRes = R.string.dcc_ticketing_consent_one_cancel_dialog_body,
                positiveButtonRes = R.string.dcc_ticketing_consent_one_cancel_dialog_continue_btn,
                negativeButtonRes = R.string.dcc_ticketing_consent_one_cancel_dialog_cancel_btn
            )
    }

    data class ErrorDialog(private val msg: String) : DccTicketingDialogType() {
        override val config: DccTicketingDialogFragment.Config
            get() = DccTicketingDialogFragment.Config(
                titleRes = R.string.errors_generic_headline_short,
                msg = msg,
                positiveButtonRes = R.string.errors_generic_button_positive
            )
    }
}
