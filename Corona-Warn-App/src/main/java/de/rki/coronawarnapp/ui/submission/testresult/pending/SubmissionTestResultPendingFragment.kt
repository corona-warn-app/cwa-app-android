package de.rki.coronawarnapp.ui.submission.testresult.pending

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultPendingBinding
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.observeOnce
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.setInvisible
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionTestResultPendingFragment : Fragment(R.layout.fragment_submission_test_result_pending), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val pendingViewModel: SubmissionTestResultPendingViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionTestResultPendingBinding by viewBindingLazy()

    private var skipInitialTestResultRefresh = false

    private var errorDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pendingViewModel.consentGiven.observe2(this) {
            binding.consentStatus.consent = it
        }

        pendingViewModel.testState.observe2(this) { result ->
            val hasResult = result.deviceUiState is NetworkRequestWrapper.RequestSuccessful
            binding.apply {
                submissionTestResultSection.setTestResultSection(result.deviceUiState, result.testResultReceivedDate)
                submissionTestResultSpinner.setInvisible(hasResult)
                submissionTestResultContent.setInvisible(!hasResult)
                buttonContainer.setInvisible(!hasResult)
            }
        }

        skipInitialTestResultRefresh = arguments?.getBoolean("skipInitialTestResultRefresh") ?: false

        binding.apply {
            submissionTestResultButtonPendingRefresh.setOnClickListener {
                pendingViewModel.refreshDeviceUIState()
                binding.submissionTestResultSection.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }

            submissionTestResultButtonPendingRemoveTest.setOnClickListener { removeTestAfterConfirmation() }

            submissionTestResultHeader.headerButtonBack.buttonIcon.setOnClickListener {
                navigateToMainScreen()
            }

            consentStatus.setOnClickListener { pendingViewModel.onConsentClicked() }
        }

        pendingViewModel.showRedeemedTokenWarning.observe2(this) {
            val dialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_tan_redeemed_title,
                R.string.submission_error_dialog_web_tan_redeemed_body,
                R.string.submission_error_dialog_web_tan_redeemed_button_positive
            )

            DialogHelper.showDialog(dialog)
        }

        pendingViewModel.routeToScreen.observe2(this) {
            it?.let { doNavigate(it) } ?: navigateToMainScreen()
        }

        pendingViewModel.observeTestResultToSchedulePositiveTestResultReminder()
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        pendingViewModel.refreshDeviceUIState(refreshTestResult = !skipInitialTestResultRefresh)
        skipInitialTestResultRefresh = false
        pendingViewModel.cwaWebExceptionLiveData.observeOnce(this.viewLifecycleOwner) { exception ->
            handleError(exception)
        }
    }

    override fun onPause() {
        pendingViewModel.cwaWebExceptionLiveData.removeObservers(this.viewLifecycleOwner)
        errorDialog?.dismiss()
        super.onPause()
    }

    private fun removeTestAfterConfirmation() {
        val removeTestDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_test_result_dialog_remove_test_title,
            R.string.submission_test_result_dialog_remove_test_message,
            R.string.submission_test_result_dialog_remove_test_button_positive,
            R.string.submission_test_result_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                pendingViewModel.deregisterTestFromDevice()
            }
        )
        DialogHelper.showDialog(removeTestDialog).apply {
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColorCompat(R.color.colorTextSemanticRed))
        }
    }

    private fun handleError(exception: CwaWebException) {
        errorDialog = when (exception) {
            is CwaClientError, is CwaServerError -> {
                DialogHelper.showDialog(buildErrorDialog(exception))
            }
            else -> {
                DialogHelper.showDialog(genericErrorDialog)
            }
        }
    }

    private fun navigateToMainScreen() {
        popBackStack()
    }

    private fun buildErrorDialog(exception: CwaWebException) = DialogHelper.DialogInstance(
        requireActivity(),
        R.string.submission_error_dialog_web_generic_error_title,
        getString(
            R.string.submission_error_dialog_web_generic_network_error_body,
            exception.statusCode
        ),
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
