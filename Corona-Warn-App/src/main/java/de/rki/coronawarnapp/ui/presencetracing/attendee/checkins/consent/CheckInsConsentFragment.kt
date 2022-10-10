package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CheckInsConsentFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

class CheckInsConsentFragment : Fragment(R.layout.check_ins_consent_fragment), AutoInject {

    private val binding: CheckInsConsentFragmentBinding by viewBinding()

    private val navArgs by navArgs<CheckInsConsentFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: CheckInsConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, savedState ->
            factory as CheckInsConsentViewModel.Factory
            factory.create(
                savedState = savedState,
                testType = navArgs.testType,
            )
        }
    )

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

    private fun showSkipDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.trace_location_attendee_consent_dialog_title)
            .setMessage(R.string.trace_location_attendee_consent_dialog_message)
            .setPositiveButton(R.string.trace_location_attendee_consent_dialog_positive_button) { _, _ ->
                Timber.d("showSkipDialog:Stay on CheckInsConsentFragment")
            }
            .setNegativeButton(R.string.trace_location_attendee_consent_dialog_negative_button) { _, _ ->
                viewModel.doNotShareCheckIns()
            }
            .show()
    }

    private fun showCloseDialog() {
        val closeDialogInstance = DialogHelper.DialogInstance(
            context = requireActivity(),
            title = R.string.submission_test_result_available_close_dialog_title_consent_given,
            message = R.string.submission_test_result_available_close_dialog_body_consent_given,
            positiveButton = R.string.submission_test_result_available_close_dialog_continue_button,
            negativeButton = R.string.submission_test_result_available_close_dialog_cancel_button,
            cancelable = true,
            positiveButtonFunction = {
                Timber.d("showCloseDialog:Stay on CheckInsConsentFragment")
            },
            negativeButtonFunction = { viewModel.onCancelConfirmed() }
        )
        DialogHelper.showDialog(closeDialogInstance)
    }
}
