package de.rki.coronawarnapp.ui.submission.resultavailable

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultAvailableBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionTestResultAvailableFragment : Fragment(R.layout.fragment_submission_test_result_available), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SubmissionTestResultAvailableViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionTestResultAvailableBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.consent.observe2(this) {
            if (it) {
                binding.submissionTestResultAvailableText.setText(
                    R.string.submission_test_result_available_text_consent_given
                )
            } else {
                binding.submissionTestResultAvailableText.setText(
                    R.string.submission_test_result_available_text_consent_not_given
                )
            }
            binding.submissionTestResultAvailableConsentStatus.consent = it
        }

        binding.apply {
            submissionTestResultAvailableProceedButton.setOnClickListener { vm.proceed() }
            submissionTestResultAvailableHeader.headerButtonBack.buttonIcon.setOnClickListener { vm.goBack() }
            submissionTestResultAvailableConsentStatus.setOnClickListener { vm.goConsent() }
        }

        vm.clickEvent.observe2(this) {
            when (it) {
                is SubmissionTestResultAvailableEvents.GoBack -> showCloseDialog()
                // TODO: Add navigation
                // is SubmissionTestResultAvailableEvents.GoConsent -> doNavigate(TestResultAvailableFragmentDirections.actionTestResultAvailableToSubmissionYourConsent())
                is SubmissionTestResultAvailableEvents.GoToTestResult -> {
                    // FIXME: Advance to next screen
                }
            }
        }

        vm.showPermissionRequest.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultAvailableContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun showCloseDialog() {
        var dialogTitle = R.string.submission_test_result_available_close_dialog_title_consent_not_given
        var dialogBody = R.string.submission_test_result_available_close_dialog_body_consent_not_given

        if (binding.submissionTestResultAvailableConsentStatus.consent) {
            dialogTitle = R.string.submission_test_result_available_close_dialog_title_consent_given
            dialogBody = R.string.submission_test_result_available_close_dialog_body_consent_given
        }

        val closeDialogInstance = DialogHelper.DialogInstance(
            requireActivity(),
            dialogTitle,
            dialogBody,
            R.string.submission_test_result_available_close_dialog_continue_button,
            R.string.submission_test_result_available_close_dialog_cancel_button,
            true, {
                // TODO: Add navigation
                // doNavigate(TestResultAvailableFragmentDirections.actionTestResultAvailableToMainFragment())
            }, {
                // Do nothing
            }
        )
        DialogHelper.showDialog(closeDialogInstance)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        vm.handleActivityResult(requestCode, resultCode, data)
    }
}
