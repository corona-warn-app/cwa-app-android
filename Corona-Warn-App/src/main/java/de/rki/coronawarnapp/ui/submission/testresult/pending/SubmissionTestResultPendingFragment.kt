package de.rki.coronawarnapp.ui.submission.testresult.pending

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultPendingBinding
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.ui.dialog.recycleTestDialog
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.observeOnce
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted

@AndroidEntryPoint
class SubmissionTestResultPendingFragment : Fragment(R.layout.fragment_submission_test_result_pending) {

    private val binding: FragmentSubmissionTestResultPendingBinding by viewBinding()

    private val navArgs by navArgs<SubmissionTestResultPendingFragmentArgs>()

    private val viewModel: SubmissionTestResultPendingViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionTestResultPendingViewModel.Factory
            factory.create(
                testIdentifier = navArgs.testIdentifier,
                initialUpdate = navArgs.forceTestResultUpdate,
                comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.consentGiven.observe(viewLifecycleOwner) {
            binding.consentStatus.consent = it
        }

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = navigateBackToFlowStart()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.testState.observe(viewLifecycleOwner) { result ->
            val isPcr = result.coronaTest.type == BaseCoronaTest.Type.PCR
            binding.apply {
                when (result.coronaTest) {
                    is FamilyCoronaTest -> {
                        if (isPcr) {
                            typeOfPendingTestResult.setEntryTitle(
                                requireContext().getText(R.string.submission_family_test_result_steps_added_pcr_heading)
                            )
                            pendingTestResultStepsWaiting.setEntryText(
                                getText(R.string.submission_family_test_result_pending_steps_waiting_pcr_body)
                            )
                        } else {
                            typeOfPendingTestResult.setEntryTitle(
                                requireContext().getText(R.string.submission_family_test_result_steps_added_rat_heading)
                            )
                            pendingTestResultStepsWaiting.setEntryText(
                                getText(R.string.submission_family_test_result_pending_steps_waiting_rat_body)
                            )
                        }
                        pendingTestResultStepsWaiting.setEntryTitle(
                            getText(R.string.submission_family_test_result_pending_steps_waiting_heading)
                        )
                        testResultPendingStepsCertificateInfo.setEntryTitle(
                            getText(R.string.submission_family_test_result_pending_steps_certificate_heading)
                        )
                        toolbar.title = getText(R.string.submission_test_result_headline)
                        familyMemberName.isVisible = true
                        familyMemberName.text = result.coronaTest.personName
                        testResultPendingStepsContactDiaryResult.isVisible = false
                        consentStatus.isVisible = false
                        submissionTestResultSpinner.isVisible = false
                        submissionTestResultContent.isVisible = true
                        buttonContainer.isVisible = true
                    }

                    is PersonalCoronaTest -> {
                        val hasResult = !result.coronaTest.isProcessing
                        if (isPcr) {
                            typeOfPendingTestResult.setEntryTitle(
                                requireContext().getText(R.string.submission_test_result_steps_added_heading)
                            )
                            pendingTestResultStepsWaiting.setEntryText(
                                getText(R.string.submission_test_result_pending_steps_waiting_pcr_body)
                            )
                        } else {
                            typeOfPendingTestResult.setEntryTitle(
                                requireContext().getText(R.string.submission_test_result_steps_added_rat_heading)
                            )
                            pendingTestResultStepsWaiting.setEntryText(
                                getText(R.string.submission_test_result_pending_steps_waiting_rat_body)
                            )
                        }
                        submissionTestResultSpinner.isInvisible = hasResult
                        submissionTestResultContent.isInvisible = !hasResult
                        buttonContainer.isInvisible = !hasResult
                        testResultPendingStepsContactDiaryResult.isVisible = true
                        consentStatus.isVisible = true
                    }
                }
                submissionTestResultSection.setTestResultSection(result.coronaTest)
            }
        }

        viewModel.testCertResultInfo.observe(viewLifecycleOwner) { result ->
            binding.testResultPendingStepsCertificateInfo.setEntryText(result.get(requireContext()))
        }

        binding.apply {
            submissionTestResultButtonPendingRefresh.setOnClickListener {
                viewModel.updateTestResult()
                binding.submissionTestResultSection.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }

            submissionTestResultButtonPendingRemoveTest.setOnClickListener {
                showMoveToRecycleBinDialog()
            }
            binding.toolbar.setNavigationOnClickListener { navigateBackToFlowStart() }
            consentStatus.setOnClickListener { viewModel.onConsentClicked() }
        }

        viewModel.showRedeemedTokenWarning.observe(viewLifecycleOwner) {
            displayDialog {
                title(R.string.submission_error_dialog_web_tan_redeemed_title)
                message(R.string.submission_error_dialog_web_tan_redeemed_body)
                positiveButton(R.string.submission_error_dialog_web_tan_redeemed_button_positive)
            }
        }

        viewModel.testCertResultInfo.observe(viewLifecycleOwner) {
            binding.testResultPendingStepsCertificateInfo.apply {
                setEntryText(it.get(context))
            }
        }

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(it)
            } ?: navigateBackToFlowStart()
        }
        viewModel.errorEvent.observe(viewLifecycleOwner) { displayDialog { setError(it) } }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        viewModel.cwaWebExceptionLiveData.observeOnce(viewLifecycleOwner) { handleError(it) }
    }

    override fun onPause() {
        viewModel.cwaWebExceptionLiveData.removeObservers(viewLifecycleOwner)
        super.onPause()
    }

    private fun showMoveToRecycleBinDialog() = recycleTestDialog { viewModel.moveTestToRecycleBinStorage() }

    private fun handleError(exception: Throwable) {
        when (exception) {
            is CwaClientError, is CwaServerError -> showNetworkErrorDialog()
            else -> showGenericErrorDialog()
        }
    }

    private fun navigateBackToFlowStart() {
        if (navArgs.comesFromDispatcherFragment) {
            findNavController().navigate(
                SubmissionTestResultPendingFragmentDirections
                    .actionSubmissionTestResultPendingFragmentToHomeFragment()
            )
        } else popBackStack()
    }

    private fun showNetworkErrorDialog() = displayDialog {
        title(R.string.submission_error_dialog_web_generic_error_title)
        message(R.string.submission_error_dialog_web_generic_network_error_body)
        negativeButton(R.string.submission_error_dialog_web_generic_error_button_positive) { navigateBackToFlowStart() }
    }

    private fun showGenericErrorDialog() = displayDialog {
        title(R.string.submission_error_dialog_web_generic_error_title)
        message(R.string.submission_error_dialog_web_generic_error_body)
        negativeButton(R.string.submission_error_dialog_web_generic_error_button_positive) { navigateBackToFlowStart() }
    }
}
