package de.rki.coronawarnapp.ui.submission.testresult.pending

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultPendingBinding
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.reyclebin.ui.dialog.RecycleBinDialogType
import de.rki.coronawarnapp.reyclebin.ui.dialog.show
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.observeOnce
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SubmissionTestResultPendingFragment : Fragment(R.layout.fragment_submission_test_result_pending), AutoInject {

    private val binding: FragmentSubmissionTestResultPendingBinding by viewBinding()

    private var errorDialog: AlertDialog? = null

    private val navArgs by navArgs<SubmissionTestResultPendingFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTestResultPendingViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionTestResultPendingViewModel.Factory
            factory.create(navArgs.testType, navArgs.testIdentifier, navArgs.forceTestResultUpdate)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.consentGiven.observe2(this) {
            binding.consentStatus.consent = it
        }

        viewModel.testState.observe2(this) { result ->
            val hasResult = !result.coronaTest.isProcessing
            binding.apply {
                submissionTestResultSection.setTestResultSection(result.coronaTest)
                submissionTestResultSpinner.isInvisible = hasResult
                submissionTestResultContent.isInvisible = !hasResult
                buttonContainer.isInvisible = !hasResult
            }
        }

        viewModel.testCertResultInfo.observe2(this) { result ->
            binding.testResultPendingStepsCertificateInfo.setEntryText(result.get(requireContext()))
        }

        binding.apply {
            val isPcr = navArgs.testType == BaseCoronaTest.Type.PCR
            testResultPendingStepsWaitingAntigenResult.isVisible = !isPcr
            testResultPendingStepsRatAdded.isVisible = !isPcr

            testResultPendingStepsWaitingPcrResult.isVisible = isPcr
            testResultPendingStepsPcrAdded.isVisible = isPcr
        }

        binding.apply {
            submissionTestResultButtonPendingRefresh.setOnClickListener {
                viewModel.updateTestResult()
                binding.submissionTestResultSection.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }

            submissionTestResultButtonPendingRemoveTest.setOnClickListener {
                showMoveToRecycleBinDialog()
            }
            submissionTestResultHeader.headerButtonBack.buttonIcon.setOnClickListener { navigateToMainScreen() }
            consentStatus.setOnClickListener { viewModel.onConsentClicked() }
        }

        viewModel.showRedeemedTokenWarning.observe2(this) {
            val dialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_tan_redeemed_title,
                R.string.submission_error_dialog_web_tan_redeemed_body,
                R.string.submission_error_dialog_web_tan_redeemed_button_positive
            )

            DialogHelper.showDialog(dialog)
        }

        viewModel.testCertResultInfo.observe2(this) {
            binding.testResultPendingStepsCertificateInfo.apply {
                setEntryText(it.get(context))
            }
        }

        viewModel.routeToScreen.observe2(this) { it?.let { doNavigate(it) } ?: navigateToMainScreen() }
        viewModel.errorEvent.observe2(this) { it.toErrorDialogBuilder(requireContext()).show() }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        viewModel.cwaWebExceptionLiveData.observeOnce(viewLifecycleOwner) { handleError(it) }
    }

    override fun onPause() {
        viewModel.cwaWebExceptionLiveData.removeObservers(viewLifecycleOwner)
        errorDialog?.dismiss()
        super.onPause()
    }

    private fun showMoveToRecycleBinDialog() {
        RecycleBinDialogType.RecycleTestConfirmation.show(
            fragment = this,
            positiveButtonAction = { viewModel.moveTestToRecycleBinStorage() }
        )
    }

    private fun handleError(exception: Throwable) {
        val dialogInstance = when (exception) {
            is CwaClientError, is CwaServerError -> networkErrorDialog
            else -> genericErrorDialog
        }
        errorDialog = DialogHelper.showDialog(dialogInstance)
    }

    private fun navigateToMainScreen() {
        popBackStack()
    }

    private val networkErrorDialog: DialogHelper.DialogInstance
        get() = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_error_dialog_web_generic_error_title,
            R.string.submission_error_dialog_web_generic_network_error_body,
            R.string.submission_error_dialog_web_generic_error_button_positive,
            null,
            true,
            ::navigateToMainScreen
        )

    private val genericErrorDialog: DialogHelper.DialogInstance
        get() = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_error_dialog_web_generic_error_title,
            R.string.submission_error_dialog_web_generic_error_body,
            R.string.submission_error_dialog_web_generic_error_button_positive,
            null,
            true,
            ::navigateToMainScreen
        )
}
