package de.rki.coronawarnapp.util

import android.app.Activity
import androidx.appcompat.app.AlertDialog

object DialogHelper {

    data class DialogInstance(
        val activity: Activity,
        val title: String,
        val message: String?,
        val positiveButton: String,
        val negativeButton: String? = null,
        val positiveButtonFunction: () -> Unit? = {},
        val negativeButtonFunction: () -> Unit? = {}
    ) {
        constructor(
            activity: Activity,
            title: Int,
            message: Int,
            positiveButton: Int,
            negativeButton: Int? = null,
            positiveButtonFunction: () -> Unit? = {},
            negativeButtonFunction: () -> Unit? = {}
        ) : this(
            activity,
            activity.resources.getString(title),
            activity.resources.getString(message),
            activity.resources.getString(positiveButton),
            negativeButton?.let { activity.resources.getString(it) },
            positiveButtonFunction,
            negativeButtonFunction
        )
    }

    fun showDialog(
        dialogInstance: DialogInstance
    ) {
        val alertDialog: AlertDialog = dialogInstance.activity.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(dialogInstance.title)
                setMessage(dialogInstance.message)
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
    }
}
