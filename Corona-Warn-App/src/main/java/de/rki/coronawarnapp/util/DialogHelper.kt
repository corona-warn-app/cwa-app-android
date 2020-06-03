package de.rki.coronawarnapp.util

import android.app.Activity
import androidx.appcompat.app.AlertDialog

object DialogHelper {

    data class DialogInstance(
        val activity: Activity,
        val title: Int,
        val message: Int,
        val positiveButton: Int,
        val negativeButton: Int? = null,
        val cancelable: Boolean = true,
        val positiveButtonFunction: () -> Unit? = {},
        val negativeButtonFunction: () -> Unit? = {}
    )

    fun showDialog(
        dialogInstance: DialogInstance
    ): AlertDialog {
        val alertDialog: AlertDialog = dialogInstance.activity.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(dialogInstance.title)
                setMessage(dialogInstance.message)
                setCancelable(dialogInstance.cancelable)
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
}
