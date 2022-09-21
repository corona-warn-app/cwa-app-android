package de.rki.coronawarnapp.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat

class DialogFragmentTemplate(
    private val cancelable: Boolean = true,
    private val isDeleteDialog: Boolean = false,
    private val dismissAction: () -> Unit = { },
    private val materialDialog: MaterialAlertDialogBuilder? = null,
    private val config: MaterialAlertDialogBuilder.() -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = cancelable
        return materialDialog?.create() ?: MaterialAlertDialogBuilder(requireContext()).apply(config).create()
    }

    override fun onStart() {
        super.onStart()
        if (isDeleteDialog) {
            (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
                ?.setTextColor(requireContext().getColorCompat(R.color.colorTextDeleteButtonDialog))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissAction()
        super.onDismiss(dialog)
    }

    companion object {
        val TAG: String = DialogFragmentTemplate::class.java.simpleName
    }
}
