package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.BugreportingDebuglogUploadFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DebugLogUploadFragment : Fragment(R.layout.bugreporting_debuglog_upload_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: DebugLogUploadViewModel by cwaViewModels { viewModelFactory }
    private val binding: BugreportingDebuglogUploadFragmentBinding by viewBinding()
    private lateinit var uploadDialog: LogUploadBlockingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadDialog = LogUploadBlockingDialog(requireContext())

        binding.apply {
            uploadAction.setOnClickListener {
                vm.onUploadLog()
            }

            debugLogPrivacyInformation.setOnClickListener {
                vm.onPrivacyButtonPress()
            }

            toolbar.setNavigationOnClickListener { popBackStack() }
        }

        vm.routeToScreen.observe2(this) {
            when (it) {
                null -> popBackStack()
                else -> findNavController().navigate(it)
            }
        }

        vm.errorEvent.observe2(this) {
            displayDialog {
                title(getString(R.string.errors_generic_headline))
                message(R.string.debugging_debuglog_share_try_again_later)
            }
        }

        vm.uploadInProgress.observe2(this) { uploadDialog.setState(it) }
        vm.uploadSuccess.observe2(this) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
