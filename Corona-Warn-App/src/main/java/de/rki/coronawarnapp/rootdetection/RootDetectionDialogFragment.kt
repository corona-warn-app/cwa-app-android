package de.rki.coronawarnapp.rootdetection

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl

class RootDetectionDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.root_dialog_title)
        .setMessage(R.string.root_dialog_message)
        .setPositiveButton(R.string.root_dialog_button) { _, _ ->
            setFragmentResult(REQUEST_KEY, bundleOf())
        }
        .setNegativeButton(R.string.root_dialog_faq_link_label, null)
        .create()
        .also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setOnClickListener { openUrl(R.string.root_dialog_faq_link_url) }
            }
        }

    companion object {
        fun newInstance() = RootDetectionDialogFragment()
    }
}

private const val REQUEST_KEY = "RootDetectionDialogFragment_RequestKey"
private const val TAG = "RootDetectionDialogFragment"

fun AppCompatActivity.showRootDetectionDialog(onPositiveAction: () -> Unit) {
    val dialog = RootDetectionDialogFragment.newInstance()
    supportFragmentManager.setFragmentResultListener(
        REQUEST_KEY,
        this
    ) { _, _ -> onPositiveAction() }
    dialog.isCancelable = false
    dialog.show(supportFragmentManager, TAG)
}
