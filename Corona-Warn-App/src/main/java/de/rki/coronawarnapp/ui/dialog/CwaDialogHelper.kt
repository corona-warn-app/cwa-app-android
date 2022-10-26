package de.rki.coronawarnapp.ui.dialog

import androidx.fragment.app.Fragment

/**
 * Helper function that creates a CwaDialog and displays it.
 * Usage:
 *
 * ```kotlin
 * createDialog {
 *     title { "Title example" }
 *     message { "Message example" }
 *     positiveButton {
 *         text { "Positive button text" }
 *         action { foo1() }
 *     }
 *     negativeButton {
 *         text { "Negative button text" }
 *         action { foo2() }
 *     }
 *     neutralButton {
 *         text { "Neutral button text" }
 *         action { foo3() }
 *     }
 *     dismissAction { foo4() }
 *     isCancelable { false }
 *     isDeleteDialog { true }
 *     customView { R.id.dialog_view }
 * }
 * ```
 */
fun Fragment.createDialog(
    lambda: CwaDialogConfigBuilder.() -> Unit
) {
    val cwaDialog = CwaDialogConfigBuilder().apply(lambda).build()
    val texts = cwaDialog.first
    val actions = cwaDialog.second
    val options = cwaDialog.third
    val positiveButtonAction = actions.positiveAction
    val negativeButtonAction = actions.negativeAction
    val neutralButtonAction = actions.neutralAction
    val dismissAction = actions.dismissAction
    val dialog = CwaDialogFragment.newInstance(texts, options)
    childFragmentManager.also {
        it.setFragmentResultListener(CwaDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            val action = bundle.getSerializable(
                CwaDialogFragment.PARAM_DIALOG_ACTION
            ) as? CwaDialogFragment.Action

            requireNotNull(action) { "Action is null" }

            when (action) {
                CwaDialogFragment.Action.PositiveButtonClicked -> positiveButtonAction()
                CwaDialogFragment.Action.NegativeButtonClicked -> negativeButtonAction()
                CwaDialogFragment.Action.NeutralButtonClicked -> neutralButtonAction()
                CwaDialogFragment.Action.Dismissed -> dismissAction()
            }
        }

        dialog.show(it, CwaDialogFragment.TAG)
    }
}
