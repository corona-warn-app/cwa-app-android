package de.rki.coronawarnapp.reyclebin.ui.dialog

import androidx.fragment.app.Fragment

typealias DialogAction = () -> Unit

/**
 * Display the dialog and register actions for button clicks or dismissal
 */
fun RecycleBinDialogType.show(
    fragment: Fragment,
    positiveButtonAction: DialogAction,
    negativeButtonAction: DialogAction = { },
    dismissAction: DialogAction = { }
) = fragment.showRecyclerDialog(config, positiveButtonAction, negativeButtonAction, dismissAction)

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
