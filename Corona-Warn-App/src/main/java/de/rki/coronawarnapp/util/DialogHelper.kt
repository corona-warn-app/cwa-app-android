package de.rki.coronawarnapp.util

import android.app.Activity
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.rki.coronawarnapp.R
import java.util.regex.Pattern

object DialogHelper {

    data class DialogInstance(
        val activity: Activity,
        val title: String,
        val message: String?,
        val positiveButton: String,
        val negativeButton: String? = null,
        val cancelable: Boolean? = true,
        val positiveButtonFunction: () -> Unit? = {},
        val negativeButtonFunction: () -> Unit? = {}
    ) {
        constructor(
            activity: Activity,
            title: Int,
            message: Int,
            positiveButton: Int,
            negativeButton: Int? = null,
            cancelable: Boolean? = true,
            positiveButtonFunction: () -> Unit? = {},
            negativeButtonFunction: () -> Unit? = {}
        ) : this(
            activity,
            activity.resources.getString(title),
            activity.resources.getString(message),
            activity.resources.getString(positiveButton),
            negativeButton?.let { activity.resources.getString(it) },
            cancelable,
            positiveButtonFunction,
            negativeButtonFunction
        )

        constructor(
            activity: Activity,
            title: Int,
            message: String,
            positiveButton: Int,
            negativeButton: Int? = null,
            cancelable: Boolean? = true,
            positiveButtonFunction: () -> Unit? = {},
            negativeButtonFunction: () -> Unit? = {}
        ) : this(
            activity,
            activity.resources.getString(title),
            message,
            activity.resources.getString(positiveButton),
            negativeButton?.let { activity.resources.getString(it) },
            cancelable,
            positiveButtonFunction,
            negativeButtonFunction
        )
    }

    fun showDialog(
        dialogInstance: DialogInstance
    ): AlertDialog {
        val message = getMessage(dialogInstance.activity, dialogInstance.message)
        val alertDialog: AlertDialog = dialogInstance.activity.let {
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

    private fun getMessage(activity: Activity, message: String?): TextView {
        // create spannable and add links, removed stack trace links into nowhere
        val spannable = SpannableString(message)
        val httpPattern: Pattern = Pattern.compile("[a-z]+://[^ \\n]*")
        Linkify.addLinks(spannable, httpPattern, "")
        // get padding for all sides
        val paddingStartEnd = activity.resources.getDimension(R.dimen.spacing_normal).toInt()
        val paddingLeftRight = activity.resources.getDimension(R.dimen.spacing_small).toInt()
        // create a textview with clickable links from the spannable
        val textView = TextView(activity)
        textView.text = spannable
        textView.linksClickable = true
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setPadding(paddingStartEnd, paddingLeftRight, paddingStartEnd, paddingLeftRight)
        textView.setTextAppearance(R.style.body1)
        textView.setLinkTextColor(activity.getColorStateList(R.color.button_primary))
        return textView
    }
}
