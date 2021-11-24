package de.rki.coronawarnapp.dccticketing.ui.dialog

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.reyclebin.ui.dialog.DialogAction

fun DccTicketingDialogType.show(
    fragment: Fragment,
    positiveButtonAction: DialogAction = { },
    negativeButtonAction: DialogAction = { },
    dismissAction: DialogAction = { }
) = fragment.showDccTicketingDialog(config, positiveButtonAction, negativeButtonAction, dismissAction)

private fun Fragment.showDccTicketingDialog(
    dialogConfig: DccTicketingDialogFragment.Config,
    positiveButtonAction: DialogAction,
    negativeButtonAction: DialogAction,
    dismissAction: DialogAction
) {
    val dialog = DccTicketingDialogFragment.newInstance(dialogConfig)
    childFragmentManager.also {
        it.setFragmentResultListener(DccTicketingDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            val action = bundle.getSerializable(
                DccTicketingDialogFragment.PARAM_DIALOG_ACTION
            ) as? DccTicketingDialogFragment.Action

            requireNotNull(action) { "Action is null" }

            when (action) {
                DccTicketingDialogFragment.Action.PositiveButtonClicked -> positiveButtonAction()
                DccTicketingDialogFragment.Action.NegativeButtonClicked -> negativeButtonAction()
                DccTicketingDialogFragment.Action.Dismissed -> dismissAction()
            }
        }

        dialog.show(it, DccTicketingDialogFragment.TAG)
    }
}
