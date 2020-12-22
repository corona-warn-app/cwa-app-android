package de.rki.coronawarnapp.ui.submission.warnothers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionNoConsentPositiveOtherWarningBinding
import de.rki.coronawarnapp.tracing.ui.TracingConsentDialog
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

/**
 * [SubmissionResultPositiveOtherWarningNoConsentFragment] the screen prompting the user to help by warning others of
 * their positive status, pressing the accept button provides the consent that was previously not provided.
 */
class SubmissionResultPositiveOtherWarningNoConsentFragment :
    Fragment(R.layout.fragment_submission_no_consent_positive_other_warning), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionResultPositiveOtherWarningNoConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionResultPositiveOtherWarningNoConsentViewModel.Factory
            factory.create()
        }
    )

    private val binding: FragmentSubmissionNoConsentPositiveOtherWarningBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submissionPositiveOtherWarningNoConsentButtonNext.setOnClickListener {
            viewModel.onConsentButtonClicked()
        }
        binding.submissionPositiveOtherWarningHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackPressed()
        }

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            doNavigate(it)
        }

        viewModel.showPermissionRequest.observe(viewLifecycleOwner) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }

        viewModel.showEnableTracingEvent.observe(viewLifecycleOwner) {
            val tracingRequiredDialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_test_result_dialog_tracing_required_title,
                R.string.submission_test_result_dialog_tracing_required_message,
                R.string.submission_test_result_dialog_tracing_required_button
            )
            DialogHelper.showDialog(tracingRequiredDialog)
        }

        binding.submissionConsentMainBottomBody.setOnClickListener {
            viewModel.onDataPrivacyClick()
        }

        viewModel.countryList.observe(viewLifecycleOwner) {
            binding.countryList.countries = it
        }

        viewModel.showTracingConsentDialog.observe(viewLifecycleOwner) { onConsentResult ->
            TracingConsentDialog(requireContext()).show(
                onConsentGiven = { onConsentResult(true) },
                onConsentDeclined = { onConsentResult(false) }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionPositiveOtherPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.handleActivityRersult(requestCode, resultCode, data)
    }
}
