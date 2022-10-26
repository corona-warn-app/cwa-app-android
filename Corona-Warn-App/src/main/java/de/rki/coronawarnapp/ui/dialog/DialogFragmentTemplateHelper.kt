package de.rki.coronawarnapp.ui.dialog

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat

fun Fragment.displayDialog(
    cancelable: Boolean = true,
    isDeleteDialog: Boolean = false,
    onDismissAction: () -> Unit = { },
    dialog: MaterialAlertDialogBuilder? = null,
    config: MaterialAlertDialogBuilder.() -> Unit = { }
) {
    /*
        TODO fix serialization by https://jira-ibs.wbs.net.sap/browse/EXPOSUREAPP-14210
    DialogFragmentTemplate.newInstance(
        DialogFragmentTemplate.DialogTemplateParams(
            cancelable,
            isDeleteDialog,
            onDismissAction,
            dialog,
            config
        )
    ).show(childFragmentManager, DialogFragmentTemplate.TAG)*/

    val alertDialogBuilder = dialog ?: MaterialAlertDialogBuilder(requireContext())
    val alertDialog = alertDialogBuilder.apply {
        config()
        setCancelable(cancelable)
        setOnDismissListener {
            onDismissAction()
        }
    }.create()
    alertDialog.show()

    if (isDeleteDialog) {
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(
            requireContext().getColorCompat(R.color.colorTextDeleteButtonDialog)
        )
    }
}

fun AppCompatActivity.displayDialog(
    cancelable: Boolean = true,
    isDeleteDialog: Boolean = false,
    onDismissAction: () -> Unit = { },
    dialog: MaterialAlertDialogBuilder? = null,
    config: MaterialAlertDialogBuilder.() -> Unit = { }
) {
    /*
    TODO fix serialization by https://jira-ibs.wbs.net.sap/browse/EXPOSUREAPP-14210
    DialogFragmentTemplate.newInstance(
         DialogFragmentTemplate.DialogTemplateParams(
             cancelable,
             isDeleteDialog,
             onDismissAction,
             dialog,
             config
         )
     ).show(supportFragmentManager, DialogFragmentTemplate.TAG)*/

    val alertDialogBuilder = dialog ?: MaterialAlertDialogBuilder(this)
    val alertDialog = alertDialogBuilder.apply {
        config()
        setCancelable(cancelable)
        setOnDismissListener {
            onDismissAction()
        }
    }.create()
    alertDialog.show()

    if (isDeleteDialog) {
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(
            getColorCompat(R.color.colorTextDeleteButtonDialog)
        )
    }
}
