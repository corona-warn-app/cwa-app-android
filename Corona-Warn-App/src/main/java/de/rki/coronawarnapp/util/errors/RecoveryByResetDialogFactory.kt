package de.rki.coronawarnapp.util.errors

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl

class RecoveryByResetDialogFactory(private val fragment: Fragment) {

    private val context: Context
        get() = fragment.requireContext()

    fun showDialog(
        @StringRes detailsLink: Int,
        onPositive: () -> Unit
    ) {
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.errors_generic_headline)
            .setMessage(R.string.errors_generic_text_catastrophic_error_recovery_via_reset)
            .setCancelable(false)
            .setNeutralButton(R.string.errors_generic_button_negative, null)
            .setPositiveButton(R.string.errors_generic_button_positive) { _, _ ->
                onPositive()
            }
            .create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL)?.setOnClickListener {
            fragment.openUrl(context.getString(detailsLink))
        }
    }
}
