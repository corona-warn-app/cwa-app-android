package de.rki.coronawarnapp.bugreporting.ui

import android.content.Context
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorStateListCompat
import de.rki.coronawarnapp.util.tryHumanReadableError
import java.util.regex.Pattern

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

    val textView = TextView(context).apply {
        text = spannable
        linksClickable = true
        movementMethod = LinkMovementMethod.getInstance()
        setPadding(
            paddingStartEnd,
            paddingLeftRight,
            paddingStartEnd,
            paddingLeftRight
        )
        TextViewCompat.setTextAppearance(this, R.style.body1)
        setLinkTextColor(context.getColorStateListCompat(R.color.button_primary))
        setTextIsSelectable(!textHasLinks)
    }
    setView(textView)
}

fun Throwable.toErrorDialogBuilder(context: Context) =
    MaterialAlertDialogBuilder(context).apply {
        val error = this@toErrorDialogBuilder
        val humanReadable = error.tryHumanReadableError(context)

        setTitle(humanReadable.title ?: context.getString(R.string.errors_generic_headline_short))
        setMessageView(humanReadable.description, textHasLinks = true)

        setPositiveButton(R.string.errors_generic_button_positive) { _, _ -> }

        setNeutralButton(R.string.errors_generic_button_negative) { _, _ ->
            MaterialAlertDialogBuilder(context).apply {
                setMessageView(
                    error.toString() + "\n\n" + error.stackTraceToString(),
                    textHasLinks = false
                )
            }.show()
        }
    }

fun Throwable.toErrorDialogBuilderNoDetails(context: Context) =
    MaterialAlertDialogBuilder(context).apply {
        val error = this@toErrorDialogBuilderNoDetails
        val humanReadable = error.tryHumanReadableError(context)

        setTitle(humanReadable.title ?: context.getString(R.string.errors_generic_headline_short))
        setMessageView(humanReadable.description, textHasLinks = true)

        setPositiveButton(R.string.errors_generic_button_positive) { _, _ -> }
    }
