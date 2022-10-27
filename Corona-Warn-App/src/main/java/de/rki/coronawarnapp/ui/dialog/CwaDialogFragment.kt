package de.rki.coronawarnapp.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import timber.log.Timber

class CwaDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val texts = requireArguments().getParcelable<CwaDialogTexts>(CWA_DIALOG_TEXTS)
        val options = requireArguments().getParcelable<CwaDialogOptions>(CWA_DIALOG_OPTIONS)
        requireNotNull(texts) { "Could not retrieve dialog texts. Result is null" }
        requireNotNull(options) { "Could not retrieve dialog options. Result is null" }
        isCancelable = options.isCancelable

        return texts.run {
            MaterialAlertDialogBuilder(requireContext()).apply {
                when (title) {
                    is IntOrString.IntResource -> setTitle(title.intResource)
                    is IntOrString.StringResource -> setTitle(title.stringResource)
                }
                when (message) {
                    is IntOrString.IntResource -> setMessage(message.intResource)
                    is IntOrString.StringResource -> setMessage(message.stringResource)
                }
                when (positiveButtonText) {
                    is IntOrString.IntResource -> setPositiveButton(positiveButtonText.intResource) { _, _ ->
                        setAction(Action.PositiveButtonClicked)
                    }
                    is IntOrString.StringResource -> setPositiveButton(positiveButtonText.stringResource) { _, _ ->
                        setAction(Action.PositiveButtonClicked)
                    }
                }
                when (negativeButtonText) {
                    is IntOrString.IntResource -> setNegativeButton(negativeButtonText.intResource) { _, _ ->
                        setAction(Action.NegativeButtonClicked)
                    }
                    is IntOrString.StringResource -> setNegativeButton(negativeButtonText.stringResource) { _, _ ->
                        setAction(Action.NegativeButtonClicked)
                    }
                }
                when (neutralButtonText) {
                    is IntOrString.IntResource -> setNeutralButton(neutralButtonText.intResource) { _, _ ->
                        setAction(Action.NeutralButtonClicked)
                    }
                    is IntOrString.StringResource -> setNeutralButton(neutralButtonText.stringResource) { _, _ ->
                        setAction(Action.NeutralButtonClicked)
                    }
                }
                options.customView?.let { setView(it) }
            }.create()
        }
    }

    private fun setAction(action: Action) {
        Timber.d("setAction(action=%s)", action)
        setFragmentResult(REQUEST_KEY, bundleOf(PARAM_DIALOG_ACTION to action))
    }

    override fun onStart() {
        super.onStart()
        val options = requireArguments().getParcelable<CwaDialogOptions>(
            CWA_DIALOG_OPTIONS
        )
        if (options?.isDeleteDialog == true) {
            (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
                ?.setTextColor(requireContext().getColorCompat(R.color.colorTextDeleteButtonDialog))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        setAction(Action.Dismissed)
        super.onDismiss(dialog)
    }

    enum class Action {
        PositiveButtonClicked,
        NegativeButtonClicked,
        NeutralButtonClicked,
        Dismissed
    }

    companion object {
        val TAG: String = CwaDialogFragment::class.java.simpleName
        val REQUEST_KEY = "${TAG}_REQUEST_KEY"
        val PARAM_DIALOG_ACTION = "${TAG}_PARAM_DIALOG_ACTION"
        private val CWA_DIALOG_TEXTS = "${TAG}_CWA_DIALOG_TEXTS"
        private val CWA_DIALOG_OPTIONS = "${TAG}_CWA_DIALOG_OPTIONS"

        fun newInstance(texts: CwaDialogTexts, options: CwaDialogOptions) = CwaDialogFragment().apply {
            arguments = bundleOf(CWA_DIALOG_TEXTS to texts, CWA_DIALOG_OPTIONS to options)
        }
    }
}
