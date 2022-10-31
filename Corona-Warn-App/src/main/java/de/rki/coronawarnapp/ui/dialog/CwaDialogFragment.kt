package de.rki.coronawarnapp.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getColorStateListCompat
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.tryHumanReadableError
import timber.log.Timber
import java.util.regex.Pattern

class CwaDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val texts = requireArguments().getParcelable<CwaDialogTexts>(CWA_DIALOG_TEXTS)
        val options = requireArguments().getParcelable<CwaDialogOptions>(CWA_DIALOG_OPTIONS)
        requireNotNull(texts) { "Could not retrieve dialog texts. Result is null" }
        requireNotNull(options) { "Could not retrieve dialog options. Result is null" }
        isCancelable = options.isCancelable

        val builder = MaterialAlertDialogBuilder(requireContext())
        return builder.apply {
            if (options.throwableError != null) {
                val humanReadableError = options.throwableError.tryHumanReadableError(requireContext())
                setErrorDialog(humanReadableError)
            }
            when (texts.title) {
                is IntOrString.IntResource -> setTitle(texts.title.intResource)
                is IntOrString.StringResource -> setTitle(texts.title.stringResource)
            }
            when (texts.message) {
                is IntOrString.IntResource -> setMessage(texts.message.intResource)
                is IntOrString.StringResource -> setMessage(texts.message.stringResource)
            }
            when (texts.positiveButtonText) {
                is IntOrString.IntResource -> setPositiveButton(texts.positiveButtonText.intResource) { _, _ ->
                    setAction(Action.PositiveButtonClicked)
                    dismiss()
                }
                is IntOrString.StringResource -> setPositiveButton(texts.positiveButtonText.stringResource) { _, _ ->
                    setAction(Action.PositiveButtonClicked)
                    dismiss()
                }
            }
            when (texts.negativeButtonText) {
                is IntOrString.IntResource -> setNegativeButton(texts.negativeButtonText.intResource) { _, _ ->
                    setAction(Action.NegativeButtonClicked)
                    dismiss()
                }
                is IntOrString.StringResource -> setNegativeButton(texts.negativeButtonText.stringResource) { _, _ ->
                    setAction(Action.NegativeButtonClicked)
                    dismiss()
                }
            }
            when (texts.neutralButtonText) {
                is IntOrString.IntResource -> setNeutralButton(texts.neutralButtonText.intResource) { _, _ ->
                    setAction(Action.NeutralButtonClicked)
                    dismiss()
                }
                is IntOrString.StringResource -> setNeutralButton(texts.neutralButtonText.stringResource) { _, _ ->
                    setAction(Action.NeutralButtonClicked)
                    dismiss()
                }
            }
            options.customView?.let { setView(it) }
        }.create()
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

    private fun MaterialAlertDialogBuilder.setErrorDialog(humanReadableError: HumanReadableError) {
        val title = context.getString(R.string.errors_generic_headline_short)
        setTitle(humanReadableError.title ?: title)
        setMessageView(humanReadableError.description, textHasLinks = true)
        setPositiveButton(R.string.errors_generic_button_positive) { _, _ -> }
    }

    private fun MaterialAlertDialogBuilder.setMessageView(
        message: String,
        textHasLinks: Boolean,
    ) {
        // create spannable and add links, removed stack trace links into nowhere
        val spannable = SpannableString(message)
        val httpPattern: Pattern = Pattern.compile("[a-z]+://[^ \\n]*")
        Linkify.addLinks(spannable, httpPattern, "")

        val paddingStartEnd = context.resources.getDimension(R.dimen.spacing_normal).toInt()
        val paddingLeftRight = context.resources.getDimension(R.dimen.spacing_small).toInt()

        val textView = TextView(context, null, R.style.TextAppearance_AppCompat_Subhead).apply {
            text = spannable
            linksClickable = true
            movementMethod = LinkMovementMethod.getInstance()
            setPadding(
                paddingStartEnd,
                paddingLeftRight,
                paddingStartEnd,
                paddingLeftRight
            )
            setLinkTextColor(context.getColorStateListCompat(R.color.button_primary))
            setTextIsSelectable(!textHasLinks)
        }
        setView(textView)
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
