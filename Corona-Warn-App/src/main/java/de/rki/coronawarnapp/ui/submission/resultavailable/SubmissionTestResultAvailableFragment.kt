package de.rki.coronawarnapp.ui.submission.resultavailable

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultAvailableBinding
import de.rki.coronawarnapp.tracing.ui.tracingConsentDialog
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * The [SubmissionTestResultAvailableFragment] appears when the user's test result is available,
 * providing the option to navigate to the consent screen where they can provide or revoke consent
 */

@AndroidEntryPoint
class SubmissionTestResultAvailableFragment : Fragment(R.layout.fragment_submission_test_result_available) {

    @Inject lateinit var appShortcutsHelper: AppShortcutsHelper
    @Inject lateinit var factory: SubmissionTestResultAvailableViewModel.Factory

    private lateinit var keyRetrievalProgress: SubmissionBlockingDialog

    private val binding: FragmentSubmissionTestResultAvailableBinding by viewBinding()
    private val navArgs by navArgs<SubmissionTestResultAvailableFragmentArgs>()
    private val viewModel: SubmissionTestResultAvailableViewModel by assistedViewModel {
        factory.create(navArgs.testIdentifier, navArgs.comesFromDispatcherFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keyRetrievalProgress = SubmissionBlockingDialog(requireContext())

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.goBack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.consent.observe(viewLifecycleOwner) {
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

        viewModel.showKeysRetrievalProgress.observe(viewLifecycleOwner) { show ->
            Timber.i("SubmissionTestResult:showKeyRetrievalProgress:$show")
            keyRetrievalProgress.setState(show)
            binding.submissionTestResultAvailableProceedButton.isEnabled = !show
        }

        binding.apply {
            submissionTestResultAvailableProceedButton.setOnClickListener { viewModel.proceed() }
            submissionTestResultAvailableConsentStatus.setOnClickListener { viewModel.goConsent() }
            toolbar.setNavigationOnClickListener { viewModel.goBack() }
        }

        viewModel.showCloseDialog.observe(viewLifecycleOwner) {
            showCloseDialog()
        }

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
        }

        viewModel.showPermissionRequest.observe(viewLifecycleOwner) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
        viewModel.showTracingConsentDialog.observe(viewLifecycleOwner) { onConsentResult ->
            tracingConsentDialog(
                positiveButton = { onConsentResult(true) },
                negativeButton = { onConsentResult(false) }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultAvailableContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        appShortcutsHelper.disableAllShortcuts()
    }

    private fun showCloseDialog() = displayDialog {
        title(R.string.submission_test_result_available_close_dialog_title_consent_given)
        message(R.string.submission_test_result_available_close_dialog_body_consent_given)
        positiveButton(R.string.submission_test_result_available_close_dialog_continue_button)
        negativeButton(R.string.submission_test_result_available_close_dialog_cancel_button) {
            returnToScreenWhereUQSWasOpened()
        }
    }

    private fun returnToScreenWhereUQSWasOpened() {
        if (navArgs.comesFromDispatcherFragment) {
            findNavController().navigate(
                SubmissionTestResultAvailableFragmentDirections
                    .actionSubmissionTestResultAvailableFragmentToHomeFragment()
            )
        } else popBackStack()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.handleActivityResult(requestCode, resultCode, data)
    }
}
