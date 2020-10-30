package de.rki.coronawarnapp.ui.submission.warnothers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveOtherWarningBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SubmissionResultPositiveOtherWarningFragment :
    Fragment(R.layout.fragment_submission_positive_other_warning),
    InternalExposureNotificationPermissionHelper.Callback, AutoInject {

    private val navArgs by navArgs<SubmissionResultPositiveOtherWarningFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionResultPositiveOtherWarningViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionResultPositiveOtherWarningViewModel.Factory
            factory.create(navArgs.symptoms)
        }
    )

    private val binding: FragmentSubmissionPositiveOtherWarningBinding by viewBindingLazy()
    private lateinit var internalExposureNotificationPermissionHelper: InternalExposureNotificationPermissionHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState.observe2(this) {
            binding.uiState = it
        }

        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)

        binding.submissionPositiveOtherWarningButtonNext.setOnClickListener {
            viewModel.onWarnOthersPressed()
        }
        binding.submissionPositiveOtherWarningHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackPressed()
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToSubmissionIntro -> doNavigate(
                    SubmissionResultPositiveOtherWarningFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionDoneFragment()
                )
                is SubmissionNavigationEvents.NavigateToSubmissionDone -> doNavigate(
                    SubmissionResultPositiveOtherWarningFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionDoneFragment()
                )
                is SubmissionNavigationEvents.NavigateToTestResult -> findNavController().popBackStack()
            }
        }

        viewModel.submissionError.observe2(this) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }

        viewModel.requestKeySharing.observe2(this) {
            internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()
        }

        viewModel.showEnableTracingEvent.observe2(this) {
            val tracingRequiredDialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_test_result_dialog_tracing_required_title,
                R.string.submission_test_result_dialog_tracing_required_message,
                R.string.submission_test_result_dialog_tracing_required_button
            )
            DialogHelper.showDialog(tracingRequiredDialog)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionPositiveOtherPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun navigateToSubmissionResultFragment() = doNavigate(
        SubmissionResultPositiveOtherWarningFragmentDirections
            .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultFragment()
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        this.internalExposureNotificationPermissionHelper.onResolutionComplete(
            requestCode,
            resultCode
        )
    }

    // InternalExposureNotificationPermissionHelper - callbacks
    override fun onKeySharePermissionGranted(keys: List<TemporaryExposureKey>) {
        super.onKeySharePermissionGranted(keys)
        viewModel.onKeysShared(keys)
    }

    override fun onFailure(exception: Exception?) {
        // NOOP
    }

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
        return when (exception) {
            is BadRequestException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_paring_invalid_title,
                R.string.submission_error_dialog_web_paring_invalid_body,
                R.string.submission_error_dialog_web_paring_invalid_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
            is ForbiddenException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_tan_invalid_title,
                R.string.submission_error_dialog_web_tan_invalid_body,
                R.string.submission_error_dialog_web_tan_invalid_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
            is CwaServerError, is CwaClientError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
        }
    }
}
