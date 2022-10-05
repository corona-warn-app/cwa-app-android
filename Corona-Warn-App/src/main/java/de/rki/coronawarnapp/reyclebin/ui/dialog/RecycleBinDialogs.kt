package de.rki.coronawarnapp.reyclebin.ui.dialog

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.removeAllItemsDialog(positiveButtonAction: () -> Unit) =
    displayDialog(isDeleteDialog = true) {
        setTitle(R.string.recycle_bin_remove_all_dialog_title)
        setMessage(R.string.recycle_bin_remove_all_dialog_message)
        setPositiveButton(R.string.recycle_bin_remove_all_dialog_positive_button) { _, _ -> positiveButtonAction() }
        setNegativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button) { _, _ -> }
    }

fun Fragment.recycleCertificateDialog(positiveButtonAction: () -> Unit) =
    displayDialog(isDeleteDialog = true) {
        setTitle(R.string.recycle_bin_recycle_certificate_dialog_title)
        setMessage(R.string.recycle_bin_recycle_certificate_dialog_message)
        setPositiveButton(R.string.recycle_bin_recycle_certificate_dialog_positive_button) { _, _ ->
            positiveButtonAction()
        }
        setNegativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button) { _, _ -> }
    }

fun Fragment.recycleTestDialog(positiveButtonAction: () -> Unit) =
    displayDialog(isDeleteDialog = true) {
        setTitle(R.string.submission_test_result_dialog_move_test_to_recycle_bin_title)
        setMessage(R.string.submission_test_result_dialog_move_test_to_recycle_bin_body)
        setPositiveButton(R.string.submission_test_result_dialog_move_test_to_recycle_bin_button) { _, _ ->
            positiveButtonAction()
        }
        setNegativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button) { _, _ -> }
    }

fun Fragment.restoreCertificateDialog(positiveButtonAction: () -> Unit) =
    displayDialog {
        setTitle(R.string.recycle_bin_restore_certificate_dialog_title)
        setMessage(R.string.recycle_bin_restore_certificate_dialog_message)
        setPositiveButton(R.string.recycle_bin_restore_certificate_dialog_positive_button) { _, _ ->
            positiveButtonAction()
        }
        setNegativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button) { _, _ -> }
    }

fun Fragment.restoreTestDialog(positiveButtonAction: () -> Unit) =
    displayDialog {
        setTitle(R.string.recycle_bin_restore_test_dialog_title)
        setMessage(R.string.recycle_bin_restore_test_dialog_message)
        setPositiveButton(R.string.recycle_bin_restore_test_dialog_positive_button) { _, _ -> positiveButtonAction() }
        setNegativeButton(R.string.recycle_bin_recycle_certificate_dialog_negative_button) { _, _ -> }
    }
