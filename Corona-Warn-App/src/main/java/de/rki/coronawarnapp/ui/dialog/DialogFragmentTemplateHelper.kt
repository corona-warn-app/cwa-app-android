package de.rki.coronawarnapp.ui.dialog

import androidx.fragment.app.Fragment

typealias DialogAction = () -> Unit

fun Fragment.showDialog(
    title: String,
    message: String,
    positiveButton: String,
    positiveButtonAction: DialogAction,
    negativeButton: String? = null,
    negativeButtonAction: DialogAction = { },
    neutralButton: String? = null,
    neutralButtonAction: DialogAction = { },
    dismissAction: DialogAction = { },
    isDeleteDialog: Boolean = false,
    isCancelable: Boolean = true
) {
    val dialogConfig = DialogFragmentTemplate.Config(
        titleRes = title,
        messageRes = message,
        positiveButtonRes = positiveButton,
        negativeButtonRes = negativeButton,
        neutralButtonRes = neutralButton,
        isDeleteDialog = isDeleteDialog,
        isCancelable = isCancelable
    )
    this.displayDialog(dialogConfig, positiveButtonAction, negativeButtonAction, neutralButtonAction, dismissAction)
}

private fun Fragment.displayDialog(
    dialogConfig: DialogFragmentTemplate.Config,
    positiveButtonAction: DialogAction,
    negativeButtonAction: DialogAction,
    neutralButtonAction: DialogAction,
    dismissAction: DialogAction
) {
    val dialog = DialogFragmentTemplate.newInstance(dialogConfig)
    childFragmentManager.also {
        it.setFragmentResultListener(DialogFragmentTemplate.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            val action = bundle.getSerializable(
                DialogFragmentTemplate.PARAM_DIALOG_ACTION,
            ) as DialogFragmentTemplate.Action

            requireNotNull(action) { "Action is null" }

            when (action) {
                DialogFragmentTemplate.Action.PositiveButtonClicked -> positiveButtonAction()
                DialogFragmentTemplate.Action.NegativeButtonClicked -> negativeButtonAction()
                DialogFragmentTemplate.Action.NeutralButtonClicked -> neutralButtonAction()
                DialogFragmentTemplate.Action.Dismissed -> dismissAction()
            }
        }

        dialog.show(it, DialogFragmentTemplate.TAG)
    }
}
