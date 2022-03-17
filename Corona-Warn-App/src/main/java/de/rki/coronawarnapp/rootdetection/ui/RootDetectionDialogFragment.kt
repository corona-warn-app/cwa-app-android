package de.rki.coronawarnapp.rootdetection.ui

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
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class RootDetectionDialogFragment : DialogFragment(), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: RootDetectionDialogViewModel by cwaViewModels { viewModelFactory }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.root_dialog_title)
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
            vm.onSuppressCheckedChanged(isChecked = isChecked)
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
