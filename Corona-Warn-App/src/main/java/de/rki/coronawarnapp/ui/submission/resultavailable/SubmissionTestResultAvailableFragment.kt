package de.rki.coronawarnapp.ui.submission.resultavailable

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultAvailableBinding
import de.rki.coronawarnapp.tracing.ui.TracingConsentDialog
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

/**
 * The [SubmissionTestResultAvailableFragment] appears when the user's test result is available,
 * providing the option to navigate to the consent screen where they can provide or revoke consent
 */
class SubmissionTestResultAvailableFragment : Fragment(R.layout.fragment_submission_test_result_available), AutoInject {

    @Inject lateinit var appShortcutsHelper: AppShortcutsHelper
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: FragmentSubmissionTestResultAvailableBinding by viewBindingLazy()
    private lateinit var keyRetrievalProgress: SubmissionBlockingDialog

    private val navArgs by navArgs<SubmissionTestResultAvailableFragmentArgs>()

    private val viewModel: SubmissionTestResultAvailableViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionTestResultAvailableViewModel.Factory
            factory.create(navArgs.testType)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keyRetrievalProgress = SubmissionBlockingDialog(requireContext())

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.goBack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.consent.observe2(this) {
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

        viewModel.showKeysRetrievalProgress.observe2(this) { show ->
            Timber.i("SubmissionTestResult:showKeyRetrievalProgress:$show")
            keyRetrievalProgress.setState(show)
            binding.submissionTestResultAvailableProceedButton.isEnabled = !show
        }

        binding.apply {
            submissionTestResultAvailableProceedButton.setOnClickListener { viewModel.proceed() }
            submissionTestResultAvailableConsentStatus.setOnClickListener { viewModel.goConsent() }
            submissionTestResultAvailableHeader.headerButtonBack.buttonIcon.setOnClickListener { viewModel.goBack() }
        }

        viewModel.showCloseDialog.observe2(this) {
            showCloseDialog()
        }

        viewModel.routeToScreen.observe2(this) {
            doNavigate(it)
        }

        viewModel.showPermissionRequest.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
        viewModel.showTracingConsentDialog.observe2(this) { onConsentResult ->
            TracingConsentDialog(requireContext()).show(
                onConsentGiven = { onConsentResult(true) },
                onConsentDeclined = { onConsentResult(false) }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultAvailableContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        appShortcutsHelper.removeAppShortcut()
    }

    private fun showCloseDialog() {
        val closeDialogInstance = DialogHelper.DialogInstance(
            context = requireActivity(),
            title = R.string.submission_test_result_available_close_dialog_title_consent_given,
            message = R.string.submission_test_result_available_close_dialog_body_consent_given,
            positiveButton = R.string.submission_test_result_available_close_dialog_continue_button,
            negativeButton = R.string.submission_test_result_available_close_dialog_cancel_button,
            cancelable = true,
            positiveButtonFunction = {},
            negativeButtonFunction = { viewModel.onCancelConfirmed() }
        )
        DialogHelper.showDialog(closeDialogInstance)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.handleActivityResult(requestCode, resultCode, data)
    }
}
