package de.rki.coronawarnapp.ui.submission.deletionwarning

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionDeletionWarningBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

class SubmissionDeletionWarningFragment : Fragment(R.layout.fragment_submission_deletion_warning), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<SubmissionDeletionWarningFragmentArgs>()
    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    private val viewModel: SubmissionDeletionWarningViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionDeletionWarningViewModel.Factory
            factory.create(args.coronaTestQrCode, args.coronaTestTan, args.isConsentGiven)
        }
    )
    private val binding: FragmentSubmissionDeletionWarningBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            when (viewModel.getTestType()) {
                CoronaTest.Type.PCR -> {
                    headline.text = getString(R.string.submission_deletion_warning_headline_pcr_test)
                    body.text = getString(R.string.submission_deletion_warning_body_pcr_test)
                }

                CoronaTest.Type.RAPID_ANTIGEN -> {
                    headline.text = getString(R.string.submission_deletion_warning_headline_antigen_test)
                    body.text = getString(R.string.submission_deletion_warning_body_antigen_test)
                }
            }

            continueButton.setOnClickListener { viewModel.deleteExistingAndRegisterNewTest() }

            toolbar.setNavigationOnClickListener { viewModel.onCancelButtonClick() }
        }

        viewModel.registrationState.observe2(this) { state ->
            binding.submissionQrCodeScanSpinner.isVisible = state.isFetching
            binding.continueButton.isVisible = !state.isFetching && state.coronaTest == null
        }
        viewModel.registrationError.observe2(this) {
            showErrorDialog(it)
            doNavigate(
                SubmissionDeletionWarningFragmentDirections
                    .actionSubmissionDeletionWarningFragmentToSubmissionDispatcherFragment()
            )
        }

        viewModel.routeToScreen.observe2(this) {
            Timber.d("Navigating to %s", it)
            doNavigate(it)
        }
    }

    private fun showErrorDialog(exception: Throwable) = when (exception) {
        is InvalidQRCodeException -> DialogHelper.DialogInstance(
            context = requireActivity(),
            title = R.string.submission_error_dialog_web_tan_redeemed_title,
            message = R.string.submission_error_dialog_web_tan_redeemed_body,
            cancelable = true,
            positiveButton = R.string.submission_error_dialog_web_tan_redeemed_button_positive,
            positiveButtonFunction = { /* dismiss */ },
        ).run { DialogHelper.showDialog(this) }
        is BadRequestException -> DialogHelper.DialogInstance(
            context = requireActivity(),
            title = R.string.submission_qr_code_scan_invalid_dialog_headline,
            message = R.string.submission_qr_code_scan_invalid_dialog_body,
            cancelable = true,
            positiveButton = R.string.submission_qr_code_scan_invalid_dialog_button_positive,
            positiveButtonFunction = { /* dismiss */ },
        ).run { DialogHelper.showDialog(this) }
        is CwaClientError, is CwaServerError -> DialogHelper.DialogInstance(
            context = requireActivity(),
            title = R.string.submission_error_dialog_web_generic_error_title,
            message = R.string.submission_error_dialog_web_generic_network_error_body,
            cancelable = true,
            positiveButton = R.string.submission_error_dialog_web_generic_error_button_positive,
            positiveButtonFunction = { /* dismiss */ },
        ).run { DialogHelper.showDialog(this) }
        is CwaWebException -> DialogHelper.DialogInstance(
            context = requireActivity(),
            title = R.string.submission_error_dialog_web_generic_error_title,
            message = R.string.submission_error_dialog_web_generic_error_body,
            cancelable = true,
            positiveButton = R.string.submission_error_dialog_web_generic_error_button_positive,
            positiveButtonFunction = { /* dismiss */ },
        ).run { DialogHelper.showDialog(this) }
        else -> exception.toErrorDialogBuilder(requireContext()).show()
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
