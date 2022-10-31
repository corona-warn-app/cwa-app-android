package de.rki.coronawarnapp.covidcertificate.validation.ui.common

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.dccValidationNoInternetDialog() = displayDialog {
    title(R.string.validation_start_no_internet_dialog_title)
    message(R.string.validation_start_no_internet_dialog_msg)
    positiveButton(R.string.validation_start_no_internet_dialog_positive_button)
}
