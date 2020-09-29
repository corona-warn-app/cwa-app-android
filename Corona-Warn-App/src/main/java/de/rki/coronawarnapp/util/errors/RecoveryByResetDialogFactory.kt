package de.rki.coronawarnapp.util.errors

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ExternalActionHelper

class RecoveryByResetDialogFactory(private val fragment: Fragment) {

    private val context: Context
        get() = fragment.requireContext()

    fun showDialog(
        @StringRes detailsLink: Int,
        onDismiss: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(R.string.errors_generic_headline)
            .setMessage(R.string.errors_generic_headline) // TODO replace with correct string
            .setCancelable(false)
            .setOnDismissListener { onDismiss() }
            .setNeutralButton(R.string.errors_generic_button_negative) { _, _ ->
                ExternalActionHelper.openUrl(fragment, context.getString(detailsLink))
            }
            .setPositiveButton(R.string.errors_generic_button_positive) { _, _ -> }
            .show()
    }

}
