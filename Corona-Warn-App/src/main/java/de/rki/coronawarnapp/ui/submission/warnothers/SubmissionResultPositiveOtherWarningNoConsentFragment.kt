package de.rki.coronawarnapp.ui.submission.warnothers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionNoConsentPositiveOtherWarningBinding
import de.rki.coronawarnapp.tracing.ui.tracingConsentDialog
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

/**
 * [SubmissionResultPositiveOtherWarningNoConsentFragment] the screen prompting the user to help by warning others of
 * their positive status, pressing the accept button provides the consent that was previously not provided.
 */
class SubmissionResultPositiveOtherWarningNoConsentFragment :
    Fragment(R.layout.fragment_submission_no_consent_positive_other_warning), AutoInject {

    private val navArgs by navArgs<SubmissionResultPositiveOtherWarningNoConsentFragmentArgs>()

    @Inject lateinit var appShortcutsHelper: AppShortcutsHelper
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionResultPositiveOtherWarningNoConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionResultPositiveOtherWarningNoConsentViewModel.Factory
            factory.create(navArgs.testIdentifier, navArgs.comesFromDispatcherFragment)
        }
    )

    private val binding: FragmentSubmissionNoConsentPositiveOtherWarningBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val keysRetrievalProgress = SubmissionBlockingDialog(requireContext())

        binding.submissionPositiveOtherWarningNoConsentButtonNext.setOnClickListener {
            viewModel.onConsentButtonClicked()
        }
        binding.toolbar.setNavigationOnClickListener {
            viewModel.onBackPressed()
        }

        viewModel.navigateBack.observe2(this) {
            goBack()
        }

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onBackPressed()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.routeToScreen.observe2(this) {
            findNavController().navigate(it)
        }

        viewModel.showKeysRetrievalProgress.observe2(this) { show ->
            keysRetrievalProgress.setState(show)
            binding.submissionPositiveOtherWarningNoConsentButtonNext.isEnabled = !show
        }

        viewModel.showPermissionRequest.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }

        viewModel.showEnableTracingEvent.observe2(this) {
            displayDialog {
                setTitle(R.string.submission_test_result_dialog_tracing_required_title)
                setMessage(R.string.submission_test_result_dialog_tracing_required_message)
                setPositiveButton(R.string.submission_test_result_dialog_tracing_required_button) { _, _ -> }
            }
        }

        binding.submissionConsentMainBottomBody.setOnClickListener {
            viewModel.onDataPrivacyClick()
        }

        viewModel.countryList.observe2(this) {
            binding.countryList.countries = it
        }

        viewModel.showTracingConsentDialog.observe2(this) { onConsentResult ->
            tracingConsentDialog(
                positiveButton = { onConsentResult(true) },
                negativeButton = { onConsentResult(false) }
            )
        }
    }

    private fun goBack() {
        popBackStack()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        appShortcutsHelper.disableAllShortcuts()
        binding.submissionPositiveOtherPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.handleActivityResult(requestCode, resultCode, data)
    }
}
