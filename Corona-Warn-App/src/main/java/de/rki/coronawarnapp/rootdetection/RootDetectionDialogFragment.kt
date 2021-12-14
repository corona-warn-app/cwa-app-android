package de.rki.coronawarnapp.rootdetection

import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import timber.log.Timber

class RootDetectionDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.root_dialog_title)
        .setMessage(R.string.root_dialog_message)
        .setPositiveButton(R.string.root_dialog_button) { _, _ ->
            setFragmentResult(REQUEST_KEY, bundleOf())
        }
        .setNegativeButton(R.string.root_dialog_faq_link_label, null)
        .setView(R.layout.root_detection_dialog_checkbox_view)
        .create()
        .also { dialog ->
            dialog.addListeners()
        }

    private fun AlertDialog.addListeners() = setOnShowListener {
        getButton(AlertDialog.BUTTON_NEGATIVE)
            .setOnClickListener { openUrl(R.string.root_dialog_faq_link_url) }

        val checkbox = findViewById<CheckBox>(R.id.checkbox)
        checkbox?.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("Checked %s", isChecked)
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
