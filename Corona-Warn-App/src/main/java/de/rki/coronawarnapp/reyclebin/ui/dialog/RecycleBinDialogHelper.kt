package de.rki.coronawarnapp.reyclebin.ui.dialog

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R

typealias DialogAction = () -> Unit

object RecycleBinDialogHelper {

    fun showRemoveAllDialog(
        fragment: Fragment,
        positiveButtonAction: DialogAction,
        negativeButtonAction: DialogAction = { },
        dismissAction: DialogAction = { }
    ) {
        val config = RecycleBinDialogFragment.Config(
            titleRes = R.string.recycle_bin_remove_all_dialog_title,
            msgRes = R.string.recycle_bin_remove_all_dialog__message,
            positiveButtonRes = R.string.recycle_bin_remove_all_dialog__positive_button,
            negativeButtonRes = R.string.recycle_bin_remove_all_dialog__negative_button
        )

        fragment.showRecyclerDialog(
            dialogConfig = config,
            positiveButtonAction = positiveButtonAction,
            negativeButtonAction = negativeButtonAction,
            dismissAction
        )
    }
}

private fun Fragment.showRecyclerDialog(
    dialogConfig: RecycleBinDialogFragment.Config,
    positiveButtonAction: DialogAction,
    negativeButtonAction: DialogAction,
    dismissAction: DialogAction
) {
    val dialog = RecycleBinDialogFragment.newInstance(dialogConfig)
    childFragmentManager.also {
        it.setFragmentResultListener(RecycleBinDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            val action = bundle.getSerializable(
                RecycleBinDialogFragment.PARAM_DIALOG_ACTION
            ) as? RecycleBinDialogFragment.Action

            requireNotNull(action) { "Action is null" }

            when (action) {
                RecycleBinDialogFragment.Action.PositiveButtonClicked -> positiveButtonAction()
                RecycleBinDialogFragment.Action.NegativeButtonClicked -> negativeButtonAction()
                RecycleBinDialogFragment.Action.Dismissed -> dismissAction()
            }
        }

        dialog.show(it, RecycleBinDialogFragment.TAG)
    }
}
