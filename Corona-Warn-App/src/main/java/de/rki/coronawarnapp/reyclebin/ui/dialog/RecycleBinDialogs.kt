package de.rki.coronawarnapp.reyclebin.ui.dialog

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.removeAllItemsDialog(positiveButtonAction: () -> Unit) = displayDialog {
    title(R.string.recycle_bin_remove_all_dialog_title)
    message(R.string.recycle_bin_remove_all_dialog_message)
    positiveButton(R.string.recycle_bin_remove_all_dialog_positive_button) { positiveButtonAction() }
    negativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button)
    setDeleteDialog(true)
}

fun Fragment.recycleCertificateDialog(positiveButtonAction: () -> Unit) = displayDialog {
    title(R.string.recycle_bin_recycle_certificate_dialog_title)
    message(R.string.recycle_bin_recycle_certificate_dialog_message)
    positiveButton(R.string.recycle_bin_recycle_certificate_dialog_positive_button) { positiveButtonAction() }
    negativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button)
    setDeleteDialog(true)
}

fun Fragment.recycleTestDialog(positiveButtonAction: () -> Unit) = displayDialog {
    title(R.string.submission_test_result_dialog_move_test_to_recycle_bin_title)
    message(R.string.submission_test_result_dialog_move_test_to_recycle_bin_body)
    positiveButton(R.string.submission_test_result_dialog_move_test_to_recycle_bin_button) { positiveButtonAction() }
    negativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button)
    setDeleteDialog(true)
}

fun Fragment.restoreCertificateDialog(positiveButtonAction: () -> Unit) = displayDialog {
    title(R.string.recycle_bin_restore_certificate_dialog_title)
    message(R.string.recycle_bin_restore_certificate_dialog_message)
    positiveButton(R.string.recycle_bin_restore_certificate_dialog_positive_button) {
        positiveButtonAction()
    }
    negativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button)
}

fun Fragment.restoreTestDialog(positiveButtonAction: () -> Unit) = displayDialog {
    title(R.string.recycle_bin_restore_test_dialog_title)
    message(R.string.recycle_bin_restore_test_dialog_message)
    positiveButton(R.string.recycle_bin_restore_test_dialog_positive_button) { positiveButtonAction() }
    negativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button)
}
