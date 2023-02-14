package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CheckInsConsentFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CheckInsConsentFragment : Fragment(R.layout.check_ins_consent_fragment) {

    @Inject lateinit var factory: CheckInsConsentViewModel.Factory
    val binding: CheckInsConsentFragmentBinding by viewBinding()
    private val navArgs by navArgs<CheckInsConsentFragmentArgs>()
    private val viewModel: CheckInsConsentViewModel by assistedViewModel { savedState ->
        factory.create(
            savedState = savedState,
            testType = navArgs.testType,
        )
    }

    private val adapter = CheckInsConsentAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onCloseClick()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        with(binding) {
            checkInsRecycler.adapter = adapter
            toolbar.setNavigationOnClickListener {
                viewModel.onCloseClick()
            }
            skipButton.setOnClickListener { viewModel.onSkipClick() }
            continueButton.setOnClickListener { viewModel.shareSelectedCheckIns() }
        }

        viewModel.checkIns.observe(viewLifecycleOwner) {
            adapter.update(it)
            binding.continueButton.isEnabled = it.any { item ->
                item is SelectableCheckInVH.Item && item.checkIn.hasSubmissionConsent
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                CheckInsConsentNavigation.OpenCloseDialog -> showCloseDialog()
                CheckInsConsentNavigation.OpenSkipDialog -> showSkipDialog()
                CheckInsConsentNavigation.ToHomeFragment -> findNavController().navigate(
                    CheckInsConsentFragmentDirections.actionCheckInsConsentFragmentToMainFragment()
                )

                CheckInsConsentNavigation.ToSubmissionResultReadyFragment -> findNavController().navigate(
                    CheckInsConsentFragmentDirections.actionCheckInsConsentFragmentToSubmissionResultReadyFragment(
                        navArgs.testType
                    )
                )

                CheckInsConsentNavigation.ToSubmissionTestResultConsentGivenFragment -> findNavController().navigate(
                    CheckInsConsentFragmentDirections
                        .actionCheckInsConsentFragmentToSubmissionTestResultConsentGivenFragment(navArgs.testType)
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.setAutoSubmission()
    }

    private fun showSkipDialog() = displayDialog {
        title(R.string.trace_location_attendee_consent_dialog_title)
        message(R.string.trace_location_attendee_consent_dialog_message)
        positiveButton(R.string.trace_location_attendee_consent_dialog_positive_button) {
            Timber.d("showSkipDialog:Stay on CheckInsConsentFragment")
        }
        negativeButton(R.string.trace_location_attendee_consent_dialog_negative_button) {
            viewModel.doNotShareCheckIns()
        }
    }

    private fun showCloseDialog() = displayDialog {
        title(R.string.submission_test_result_available_close_dialog_title_consent_given)
        message(R.string.submission_test_result_available_close_dialog_body_consent_given)
        positiveButton(R.string.submission_test_result_available_close_dialog_continue_button) {
            Timber.d("showCloseDialog:Stay on CheckInsConsentFragment")
        }
        negativeButton(R.string.submission_test_result_available_close_dialog_cancel_button) {
            viewModel.onCancelConfirmed()
        }
    }
}
