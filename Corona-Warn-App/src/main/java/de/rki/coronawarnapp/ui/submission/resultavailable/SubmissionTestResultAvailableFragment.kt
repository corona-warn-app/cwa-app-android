package de.rki.coronawarnapp.ui.submission.resultavailable

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultAvailableBinding
import de.rki.coronawarnapp.tracing.ui.TracingConsentDialog
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The [SubmissionTestResultAvailableFragment] appears when the user's test result is available,
 * providing the option to navigate to the consent screen where they can provide or revoke consent
 */
class SubmissionTestResultAvailableFragment : Fragment(R.layout.fragment_submission_test_result_available), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SubmissionTestResultAvailableViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionTestResultAvailableBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = vm.goBack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

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
            submissionTestResultAvailableConsentStatus.setOnClickListener { vm.goConsent() }
            submissionTestResultAvailableHeader.headerButtonBack.buttonIcon.setOnClickListener { vm.goBack() }
        }

        vm.showCloseDialog.observe2(this) {
            showCloseDialog()
        }

        vm.routeToScreen.observe2(this) {
            doNavigate(it)
        }

        vm.showPermissionRequest.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
        vm.showTracingConsentDialog.observe2(this) { onConsentResult ->
            TracingConsentDialog(requireContext()).show(
                onConsentGiven = { onConsentResult(true) },
                onConsentDeclined = { onConsentResult(false) }
            )
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
            context = requireActivity(),
            title = dialogTitle,
            message = dialogBody,
            positiveButton = R.string.submission_test_result_available_close_dialog_continue_button,
            negativeButton = R.string.submission_test_result_available_close_dialog_cancel_button,
            cancelable = true,
            positiveButtonFunction = { vm.onCancelConfirmed() },
            negativeButtonFunction = { }
        )
        DialogHelper.showDialog(closeDialogInstance)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        vm.handleActivityResult(requestCode, resultCode, data)
    }
}
