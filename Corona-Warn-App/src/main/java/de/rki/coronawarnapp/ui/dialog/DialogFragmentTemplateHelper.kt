package de.rki.coronawarnapp.ui.dialog

import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.displayDialog(
    cancelable: Boolean = true,
    isDeleteDialog: Boolean = false,
    tag: String? = null,
    onDismissAction: () -> Unit = { },
    dialog: MaterialAlertDialogBuilder? = null,
    config: MaterialAlertDialogBuilder.() -> Unit = { }
) {
    DialogFragmentTemplate(cancelable, isDeleteDialog, onDismissAction, dialog, config).show(
        childFragmentManager,
        tag ?: DialogFragmentTemplate.TAG // if no tag is passed, we default to the tag of the template
    )
}
