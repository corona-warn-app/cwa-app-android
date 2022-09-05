package de.rki.coronawarnapp.reyclebin.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import kotlinx.parcelize.Parcelize
import timber.log.Timber

class RecycleBinDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val config = requireArguments().getParcelable<Config>(PARAM_DIALOG_CONFIG)
        requireNotNull(config) { "Dialog config is null" }

        return config.run {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleRes)
                .setMessage(msgRes)
                .setPositiveButton(positiveButtonRes) { _, _ -> setAction(Action.PositiveButtonClicked) }
                .setNegativeButton(negativeButtonRes) { _, _ -> setAction(Action.NegativeButtonClicked) }
                .create()
        }
    }

    override fun onStart() {
        super.onStart()
        val config = requireArguments().getParcelable<Config>(PARAM_DIALOG_CONFIG)
        if (config?.isDeleteDialog == true) {
            (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
                ?.setTextColor(requireContext().getColorCompat(R.color.colorTextSemanticRed))
        }
    }

    private fun setAction(action: Action) {
        Timber.d("setAction(action=%s)", action)
        setFragmentResult(REQUEST_KEY, bundleOf(PARAM_DIALOG_ACTION to action))
    }

    override fun onDismiss(dialog: DialogInterface) {
        setAction(Action.Dismissed)
        super.onDismiss(dialog)
    }

    enum class Action {
        PositiveButtonClicked,
        NegativeButtonClicked,
        Dismissed
    }

    @Parcelize
    data class Config(
        @StringRes val titleRes: Int,
        @StringRes val msgRes: Int,
        @StringRes val positiveButtonRes: Int,
        @StringRes val negativeButtonRes: Int,
        val isDeleteDialog: Boolean = false
    ) : Parcelable

    companion object {
        val TAG: String = RecycleBinDialogFragment::class.java.simpleName
        val REQUEST_KEY = "${TAG}_REQUEST_KEY"
        val PARAM_DIALOG_ACTION = "${TAG}_PARAM_DIALOG_ACTION"
        private val PARAM_DIALOG_CONFIG = "${TAG}_PARAM_DIALOG_CONFIG"

        fun newInstance(dialogConfig: Config) = RecycleBinDialogFragment().apply {
            arguments = bundleOf(PARAM_DIALOG_CONFIG to dialogConfig)
        }
    }
}
