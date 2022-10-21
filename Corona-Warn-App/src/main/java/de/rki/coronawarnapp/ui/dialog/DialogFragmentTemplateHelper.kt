package de.rki.coronawarnapp.ui.dialog

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.displayDialog(
    cancelable: Boolean = true,
    isDeleteDialog: Boolean = false,
    onDismissAction: () -> Unit = { },
    dialog: MaterialAlertDialogBuilder? = null,
    config: MaterialAlertDialogBuilder.(DialogFragment) -> Unit = { }
) {
    DialogFragmentTemplate.newInstance(
        DialogFragmentTemplate.DialogTemplateParams(
            cancelable,
            isDeleteDialog,
            onDismissAction,
            dialog,
            config
        )
    ).show(childFragmentManager, DialogFragmentTemplate.TAG)
}

fun AppCompatActivity.displayDialog(
    cancelable: Boolean = true,
    isDeleteDialog: Boolean = false,
    onDismissAction: () -> Unit = { },
    dialog: MaterialAlertDialogBuilder? = null,
    config: MaterialAlertDialogBuilder.(DialogFragment) -> Unit = { }
) {
    DialogFragmentTemplate.newInstance(
        DialogFragmentTemplate.DialogTemplateParams(
            cancelable,
            isDeleteDialog,
            onDismissAction,
            dialog,
            config
        )
    ).show(supportFragmentManager, DialogFragmentTemplate.TAG)
}
