package de.rki.coronawarnapp.reyclebin.ui.dialog

import de.rki.coronawarnapp.R

/**
 * Use the extension [RecycleBinDialogType.show] to display a dialog
 * e.g. RecycleBinDialogType.RemoveAllConfirmation.show(...)
 */
sealed class RecycleBinDialogType {
    abstract val config: RecycleBinDialogFragment.Config

    object RemoveAllConfirmation : RecycleBinDialogType() {
        override val config: RecycleBinDialogFragment.Config
            get() = RecycleBinDialogFragment.Config(
                titleRes = R.string.recycle_bin_remove_all_dialog_title,
                msgRes = R.string.recycle_bin_remove_all_dialog_message,
                positiveButtonRes = R.string.recycle_bin_remove_all_dialog_positive_button,
                negativeButtonRes = R.string.recycle_bin_remove_all_dialog_negative_button
            )
    }

    object RecycleCertificateConfirmation : RecycleBinDialogType() {
        override val config: RecycleBinDialogFragment.Config
            get() = RecycleBinDialogFragment.Config(
                titleRes = R.string.recycle_bin_recycle_certificate_dialog_title,
                msgRes = R.string.recycle_bin_recycle_certificate_dialog_message,
                positiveButtonRes = R.string.recycle_bin_recycle_certificate_dialog_positive_button,
                negativeButtonRes = R.string.recycle_bin_recycle_certificate_dialog_negative_button
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
