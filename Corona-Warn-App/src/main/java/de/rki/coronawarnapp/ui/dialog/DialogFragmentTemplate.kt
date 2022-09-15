package de.rki.coronawarnapp.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import kotlinx.parcelize.Parcelize
import timber.log.Timber

class DialogFragmentTemplate : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val config = requireArguments().getParcelable<Config>(PARAM_DIALOG_CONFIG)
        requireNotNull(config) { "DialogFragment config is null" }
        isCancelable = config.isCancelable

        return config.run {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton(positiveButtonRes) { _, _ -> setAction(Action.PositiveButtonClicked) }
                .setNegativeButton(negativeButtonRes) { _, _ -> setAction(Action.NegativeButtonClicked) }
                .setNeutralButton(neutralButtonRes) { _, _ -> setAction(Action.NeutralButtonClicked) }
                .create()
        }
    }

    override fun onStart() {
        super.onStart()
        val config = requireArguments().getParcelable<Config>(PARAM_DIALOG_CONFIG)
        if (config?.isDeleteDialog == true) {
            (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
                ?.setTextColor(requireContext().getColorCompat(R.color.colorTextDeleteButtonDialog))
        }
    }

    private fun setAction(action: Action) {
        Timber.d("DialogFragment setAction(action=%s", action)
        setFragmentResult(REQUEST_KEY, bundleOf(PARAM_DIALOG_ACTION to action))
    }

    override fun onDismiss(dialog: DialogInterface) {
        Timber.d("DialogFragment dismissed")
        setAction(Action.Dismissed)
        super.onDismiss(dialog)
    }

    enum class Action {
        PositiveButtonClicked,
        NegativeButtonClicked,
        NeutralButtonClicked,
        Dismissed
    }

    @Parcelize
    data class Config(
        val titleRes: String,
        val messageRes: String,
        val positiveButtonRes: String,
        val negativeButtonRes: String?,
        val neutralButtonRes: String?,
        val isDeleteDialog: Boolean = false,
        val isCancelable: Boolean = true
    ) : Parcelable

    companion object {
        val TAG: String = DialogFragmentTemplate::class.java.simpleName
        val REQUEST_KEY = "${TAG}_REQUEST_KEY"
        val PARAM_DIALOG_ACTION = "${TAG}_PARAM_DIALOG_ACTION"
        private val PARAM_DIALOG_CONFIG = "${TAG}_PARAM_DIALOG_CONFIG"

        fun newInstance(dialogConfig: Config) = DialogFragmentTemplate().apply {
            arguments = bundleOf(PARAM_DIALOG_CONFIG to dialogConfig)
        }
    }
}
