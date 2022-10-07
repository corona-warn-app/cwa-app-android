package de.rki.coronawarnapp.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

class DialogFragmentTemplate : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogParams = requireArguments().getParcelable<DialogTemplateParams>(DIALOG_TEMPLATE_PARAMS)
        requireNotNull(dialogParams) { "DialogTemplateParams is null" }
        isCancelable = dialogParams.cancelable == true
        val builder = dialogParams.materialDialog ?: MaterialAlertDialogBuilder(requireContext())
        return builder.apply {
            dialogParams.config(builder, this@DialogFragmentTemplate)
        }.create()
    }

    override fun onStart() {
        super.onStart()
        val dialogParams = requireArguments().getParcelable<DialogTemplateParams>(DIALOG_TEMPLATE_PARAMS)
        if (dialogParams?.isDeleteDialog == true) {
            (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
                ?.setTextColor(requireContext().getColorCompat(R.color.colorTextDeleteButtonDialog))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        val dialogParams = requireArguments().getParcelable<DialogTemplateParams>(DIALOG_TEMPLATE_PARAMS)
        dialogParams?.dismissAction?.let { it() }
        super.onDismiss(dialog)
    }

    @Parcelize
    data class DialogTemplateParams(
        val cancelable: Boolean = true,
        val isDeleteDialog: Boolean = false,
        val dismissAction: () -> Unit = { },
        val materialDialog: @RawValue MaterialAlertDialogBuilder? = null,
        val config: MaterialAlertDialogBuilder.(DialogFragment) -> Unit
    ) : Parcelable

    companion object {
        val TAG: String = DialogFragmentTemplate::class.java.simpleName
        private val DIALOG_TEMPLATE_PARAMS = "${TAG}_DIALOG_TEMPLATE_PARAMS"

        fun newInstance(dialogParams: DialogTemplateParams) = DialogFragmentTemplate().apply {
            arguments = bundleOf(DIALOG_TEMPLATE_PARAMS to dialogParams)
        }
    }
}
