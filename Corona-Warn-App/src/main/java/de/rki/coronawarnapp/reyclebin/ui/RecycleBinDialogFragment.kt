package de.rki.coronawarnapp.reyclebin.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize

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

    private fun setAction(action: Action) {
        setFragmentResult(REQUEST_KEY, bundleOf(PARAM_DIALOG_ACTION to action))
    }

    sealed class Action {
        object PositiveButtonClicked : Action()
        object NegativeButtonClicked : Action()
    }

    @Parcelize
    private data class Config(
        @StringRes val titleRes: Int,
        @StringRes val msgRes: Int,
        @StringRes val positiveButtonRes: Int,
        @StringRes val negativeButtonRes: Int,
    ) : Parcelable

    companion object {
        private val TAG: String = RecycleBinDialogFragment::class.java.simpleName
        private val PARAM_DIALOG_CONFIG = "${TAG}_PARAM_DIALOG_CONFIG"

        val REQUEST_KEY = "${TAG}_REQUEST_KEY"
        val PARAM_DIALOG_ACTION = "${TAG}_PARAM_DIALOG_ACTION"

        fun newInstance(
            @StringRes titleRes: Int,
            @StringRes msgRes: Int,
            @StringRes positiveButtonRes: Int,
            @StringRes negativeButtonRes: Int,
        ) = RecycleBinDialogFragment().apply {
            val dialogConfig = Config(titleRes, msgRes, positiveButtonRes, negativeButtonRes)
            arguments = bundleOf(PARAM_DIALOG_CONFIG to dialogConfig)
        }
    }
}
