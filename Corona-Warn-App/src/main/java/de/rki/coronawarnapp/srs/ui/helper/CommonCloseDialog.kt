package de.rki.coronawarnapp.srs.ui.helper

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.showSrsCloseDialog(closeFun: () -> Unit) {
    displayDialog {
        title(R.string.srs_cancel_dialog_title)
        message(R.string.srs_cancel_dialog_body)
        positiveButton(R.string.srs_cancel_dialog_positive_button)
        negativeButton(R.string.srs_cancel_dialog_negative_button) { closeFun() }
    }
}
