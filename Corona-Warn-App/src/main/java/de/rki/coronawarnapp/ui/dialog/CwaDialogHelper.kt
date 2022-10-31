package de.rki.coronawarnapp.ui.dialog

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Helper function that creates a CwaDialog and displays it.
 * Usage:
 *
 * ```kotlin
 * createDialog {
 *     title("Title example")
 *     message("Message example")
 *     positiveButton("Positive button text") { foo1() }
 *     negativeButton("Negative button text") { foo2() }
 *     neutralButton("Neutral button text") { foo3() }
 *     dismissAction { foo4() }
 *     setCancelable(false)
 *     setDeleteDialog(true)
 *     setCustomView(R.layout.dialog_view)
 * }
 * ```
 */
fun Fragment.displayDialog(
    lambda: CwaDialogBuilder.() -> Unit
) {
    val cwaDialog = CwaDialogBuilder().apply(lambda).build()
    val texts = cwaDialog.first
    val actions = cwaDialog.second
    val options = cwaDialog.third
    val positiveButtonAction = actions.positiveAction
    val negativeButtonAction = actions.negativeAction
    val neutralButtonAction = actions.neutralAction
    val dismissAction = actions.dismissAction
    val tag = this::class.java.simpleName + CwaDialogFragment.TAG
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

        dialog.show(it, tag)
    }
}

/**
 * Helper function that creates a CwaDialog and displays it.
 * Usage:
 *
 * ```kotlin
 * createDialog {
 *     title("Title example")
 *     message("Message example")
 *     positiveButton("Positive button text") { foo1() }
 *     negativeButton("Negative button text") { foo2() }
 *     neutralButton("Neutral button text") { foo3() }
 *     dismissAction { foo4() }
 *     setCancelable(false)
 *     setDeleteDialog(true)
 *     setCustomView(R.layout.dialog_view)
 * }
 * ```
 */
fun AppCompatActivity.displayDialog(
    lambda: CwaDialogBuilder.() -> Unit
) {
    val cwaDialog = CwaDialogBuilder().apply(lambda).build()
    val texts = cwaDialog.first
    val actions = cwaDialog.second
    val options = cwaDialog.third
    val positiveButtonAction = actions.positiveAction
    val negativeButtonAction = actions.negativeAction
    val neutralButtonAction = actions.neutralAction
    val dismissAction = actions.dismissAction
    val tag = this::class.java.simpleName + CwaDialogFragment.TAG
    val dialog = CwaDialogFragment.newInstance(texts, options)
    supportFragmentManager.also {
        it.setFragmentResultListener(CwaDialogFragment.REQUEST_KEY, this) { _, bundle ->
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

        dialog.show(it, tag)
    }
}
