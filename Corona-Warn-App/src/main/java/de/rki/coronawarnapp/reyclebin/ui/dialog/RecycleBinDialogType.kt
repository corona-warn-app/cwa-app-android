package de.rki.coronawarnapp.reyclebin.ui.dialog

import de.rki.coronawarnapp.R

/**
 * Use the extension [RecycleBinDialogType.show] to display a dialog
 * e.g. RecycleBinDialogType.RemoveAllConfirmation.show(...)
 */
sealed class RecycleBinDialogType {
    abstract val config: RecycleBinDialogFragment.Config

    object RemoveAllItemsConfirmation : RecycleBinDialogType() {
        override val config: RecycleBinDialogFragment.Config
            get() = RecycleBinDialogFragment.Config(
                titleRes = R.string.recycle_bin_remove_all_dialog_title,
                msgRes = R.string.recycle_bin_remove_all_dialog_message,
                positiveButtonRes = R.string.recycle_bin_remove_all_dialog_positive_button,
                negativeButtonRes = R.string.recycle_bin_remove_all_dialog_negative_button,
                isDeleteDialog = true
            )
    }

    object RecycleCertificateConfirmation : RecycleBinDialogType() {
        override val config: RecycleBinDialogFragment.Config
            get() = RecycleBinDialogFragment.Config(
                titleRes = R.string.recycle_bin_recycle_certificate_dialog_title,
                msgRes = R.string.recycle_bin_recycle_certificate_dialog_message,
                positiveButtonRes = R.string.recycle_bin_recycle_certificate_dialog_positive_button,
                negativeButtonRes = R.string.recycle_bin_recycle_certificate_dialog_negative_button,
                isDeleteDialog = true
            )
    }

    object RecycleTestConfirmation : RecycleBinDialogType() {
        override val config: RecycleBinDialogFragment.Config
            get() = RecycleBinDialogFragment.Config(
                titleRes = R.string.submission_test_result_dialog_move_test_to_recycle_bin_title,
                msgRes = R.string.submission_test_result_dialog_move_test_to_recycle_bin_body,
                positiveButtonRes = R.string.submission_test_result_dialog_move_test_to_recycle_bin_button,
                negativeButtonRes = R.string.recycle_bin_recycle_certificate_dialog_negative_button,
                isDeleteDialog = true
            )
    }

    object RestoreCertificateConfirmation : RecycleBinDialogType() {
        override val config: RecycleBinDialogFragment.Config
            get() = RecycleBinDialogFragment.Config(
                titleRes = R.string.recycle_bin_restore_certificate_dialog_title,
                msgRes = R.string.recycle_bin_restore_certificate_dialog_message,
                positiveButtonRes = R.string.recycle_bin_restore_certificate_dialog_positive_button,
                negativeButtonRes = R.string.recycle_bin_restore_certificate_dialog_negative_button
            )
    }

    object RestoreTestConfirmation : RecycleBinDialogType() {
        override val config: RecycleBinDialogFragment.Config
            get() = RecycleBinDialogFragment.Config(
                titleRes = R.string.recycle_bin_restore_test_dialog_title,
                msgRes = R.string.recycle_bin_restore_test_dialog_message,
                positiveButtonRes = R.string.recycle_bin_restore_test_dialog_positive_button,
                negativeButtonRes = R.string.recycle_bin_restore_test_dialog_negative_button
            )
    }
}
