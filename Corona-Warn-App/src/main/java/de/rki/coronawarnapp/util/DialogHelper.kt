package de.rki.coronawarnapp.util

import android.content.Context
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorStateListCompat
import java.util.regex.Pattern

object DialogHelper {

    data class DialogInstance(
        val context: Context,
        val title: String,
        val message: String?,
        val positiveButton: String,
        val negativeButton: String? = null,
        val cancelable: Boolean? = true,
        val isTextSelectable: Boolean = false,
        val positiveButtonFunction: () -> Unit? = {},
        val negativeButtonFunction: () -> Unit? = {}
    ) {
        constructor(
            context: Context,
            title: Int,
            message: Int,
            positiveButton: Int,
            negativeButton: Int? = null,
            cancelable: Boolean? = true,
            positiveButtonFunction: () -> Unit? = {},
            negativeButtonFunction: () -> Unit? = {}
        ) : this(
            context = context,
            title = context.resources.getString(title),
            message = context.resources.getString(message),
            positiveButton = context.resources.getString(positiveButton),
            negativeButton = negativeButton?.let { context.resources.getString(it) },
            cancelable = cancelable,
            positiveButtonFunction = positiveButtonFunction,
            negativeButtonFunction = negativeButtonFunction
        )

        constructor(
            context: Context,
            title: Int,
            message: String,
            positiveButton: Int,
            negativeButton: Int? = null,
            cancelable: Boolean? = true,
            positiveButtonFunction: () -> Unit? = {},
            negativeButtonFunction: () -> Unit? = {}
        ) : this(
            context = context,
            title = context.resources.getString(title),
            message = message,
            positiveButton = context.resources.getString(positiveButton),
            negativeButton = negativeButton?.let { context.resources.getString(it) },
            cancelable = cancelable,
            positiveButtonFunction = positiveButtonFunction,
            negativeButtonFunction = negativeButtonFunction
        )
    }

    fun showDialog(
        dialogInstance: DialogInstance
    ): AlertDialog {
        val message = getMessage(
            dialogInstance.context,
            dialogInstance.message,
            dialogInstance.isTextSelectable
        )
        val alertDialog: AlertDialog = dialogInstance.context.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(dialogInstance.title)
                setView(message)
                setCancelable(dialogInstance.cancelable ?: true)
                setPositiveButton(
                    dialogInstance.positiveButton
                ) { _, _ ->
                    dialogInstance.positiveButtonFunction()
                }
                if (dialogInstance.negativeButton != null) {
                    setNegativeButton(
                        dialogInstance.negativeButton
                    ) { _, _ ->
                        dialogInstance.negativeButtonFunction()
                    }
                }
            }
            builder.create()
        }
        alertDialog.show()
        return alertDialog
    }

    private fun getMessage(context: Context, message: String?, isTextSelectable: Boolean): TextView {
        // create spannable and add links, removed stack trace links into nowhere
        val spannable = SpannableString(message)
        val httpPattern: Pattern = Pattern.compile("[a-z]+://[^ \\n]*")
        Linkify.addLinks(spannable, httpPattern, "")
        // get padding for all sides
        val paddingStartEnd = context.resources.getDimension(R.dimen.spacing_normal).toInt()
        val paddingLeftRight = context.resources.getDimension(R.dimen.spacing_small).toInt()
        // create a textview with clickable links from the spannable
        val textView = TextView(context)
        textView.text = spannable
        textView.linksClickable = true
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setPadding(paddingStartEnd, paddingLeftRight, paddingStartEnd, paddingLeftRight)
        textView.setTextAppearance(R.style.body1)
        textView.setLinkTextColor(context.getColorStateListCompat(R.color.button_primary))
        if (isTextSelectable) textView.setTextIsSelectable(true)
        return textView
    }
}
