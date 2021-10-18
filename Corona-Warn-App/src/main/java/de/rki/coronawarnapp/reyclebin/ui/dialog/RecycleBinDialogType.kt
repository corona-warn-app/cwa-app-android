package de.rki.coronawarnapp.reyclebin.ui.dialog

import de.rki.coronawarnapp.R

sealed class RecycleBinDialogType {
    abstract val config: RecycleBinDialogFragment.Config

    object RemoveAllConfirmation : RecycleBinDialogType() {
        override val config: RecycleBinDialogFragment.Config
            get() = RecycleBinDialogFragment.Config(
                titleRes = R.string.recycle_bin_remove_all_dialog_title,
                msgRes = R.string.recycle_bin_remove_all_dialog__message,
                positiveButtonRes = R.string.recycle_bin_remove_all_dialog__positive_button,
                negativeButtonRes = R.string.recycle_bin_remove_all_dialog__negative_button
            )
    }
}
