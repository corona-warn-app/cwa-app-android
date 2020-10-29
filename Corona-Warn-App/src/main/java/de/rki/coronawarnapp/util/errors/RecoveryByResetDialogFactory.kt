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
        onPositive: () -> Unit
    ) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.errors_generic_headline)
            .setMessage(R.string.errors_generic_text_catastrophic_error_recovery_via_reset)
            .setCancelable(false)
            .setNeutralButton(R.string.errors_generic_button_negative, null)
            .setPositiveButton(R.string.errors_generic_button_positive) { _, _ ->
                onPositive()
            }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
            ExternalActionHelper.openUrl(fragment, context.getString(detailsLink))
        }
    }
}
