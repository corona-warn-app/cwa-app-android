package de.rki.coronawarnapp.dccticketing.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.util.ui.LazyString
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.lang.IllegalArgumentException

class DccTicketingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val config = requireArguments().getParcelable<Config>(PARAM_DIALOG_CONFIG)
        requireNotNull(config) { "Dialog config is null" }

        return config.run {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(titleRes)

                when {
                    msgRes != null -> setMessage(msgRes)
                    msg != null -> setMessage(msg)
                    else -> throw IllegalArgumentException("msgRes or msg must be set!")
                }

                setPositiveButton(positiveButtonRes) { _, _ -> setAction(Action.PositiveButtonClicked) }

                negativeButtonRes?.let { setNegativeButton(it) { _, _ -> setAction(Action.NegativeButtonClicked) } }
            }.create()
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
        @StringRes val msgRes: Int? = null,
        val msg: String? = null,
        @StringRes val positiveButtonRes: Int,
        @StringRes val negativeButtonRes: Int? = null,
    ) : Parcelable

    companion object {
        val TAG: String = DccTicketingDialogFragment::class.java.simpleName
        val REQUEST_KEY = "${TAG}_REQUEST_KEY"
        val PARAM_DIALOG_ACTION = "${TAG}_PARAM_DIALOG_ACTION"
        private val PARAM_DIALOG_CONFIG = "${TAG}_PARAM_DIALOG_CONFIG"

        fun newInstance(dialogConfig: Config) = DccTicketingDialogFragment().apply {
            arguments = bundleOf(PARAM_DIALOG_CONFIG to dialogConfig)
        }
    }
}
