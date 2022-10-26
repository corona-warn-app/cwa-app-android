package de.rki.coronawarnapp.ui.dialog

import androidx.fragment.app.Fragment

/**
 * Helper function that creates a CwaDialog and displays it.
 * Usage:
 *
 * ```kotlin
 * createDialog {
 *     configureTexts {
 *         title { "Title example" }
 *         message { "Message example" }
 *         positiveButton { "Positive button text" }
 *         negativeButton { "Negative button text" }
 *         neutralButton { "Neutral button text" }
 *     }
 *     configureActions {
 *         positiveAction { foo1() }
 *         negativeAction { foo2() }
 *         neutralAction { foo3() }
 *         dismissAction { foo4() }
 *     }
 *     configureOptions {
 *         isCancelable { false }
 *         isDeleteDialog { true }
 *         customView { R.id.dialog_view }
 *     }
 * }
 * ```
 * @param title The text displayed for the dialog title. Accepts Int or String values. Can be omitted.
 * @param message The text displayed for the dialog message. Accepts Int or String values. Can be omitted.
 * @param positiveButton The text displayed for the dialog's positive button. Accepts Int or String. Can be omitted.
 * @param negativeButton The text displayed for the dialog's negative button. Accepts Int or String. Can be omitted.
 * @param neutralButton The text displayed for the dialog's neutral button. Accepts Int or String. Can be omitted.
 * @param positiveAction The action executed when tne positive button is pressed. Accepts a function. Can be omitted.
 * @param negativeAction The action executed when tne negative button is pressed. Accepts a function. Can be omitted.
 * @param neutralAction The action executed when tne neutral button is pressed. Accepts a function. Can be omitted.
 * @param dismissAction The action executed when tne dialog is dismissed. Accepts a function. Can be omitted.
 * @param isCancelable Tells the dialog fragment if the dialog is dismissible or not. Accepts Boolean. Can be omitted.
 * @param isDeleteDialog Makes the positive button red when set to true. Accepts Boolean. Can be omitted.
 * @param customView Sets a custom view to the dialog fragment. Accepts Int. Can be omitted.
 *
 */
fun Fragment.createDialog(
    lambda: CwaDialogBuilder.() -> Unit
) {
    val cwaDialog = CwaDialogBuilder().apply(lambda).build()
    val texts = cwaDialog.texts
    val actions = cwaDialog.actions
    val options = cwaDialog.options
    val positiveButtonAction = actions.positiveAction
    val negativeButtonAction = actions.negativeAction
    val neutralButtonAction = actions.neutralAction
    val dismissAction = cwaDialog.actions.dismissAction
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
