package de.rki.coronawarnapp.ui.submission.deletionwarning

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionDeletionWarningBinding
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor.State
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

class SubmissionDeletionWarningFragment : Fragment(R.layout.fragment_submission_deletion_warning), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val navOptions = NavOptions.Builder().setPopUpTo(R.id.submissionDeletionWarningFragment, true).build()
    private val args by navArgs<SubmissionDeletionWarningFragmentArgs>()

    private val viewModel: SubmissionDeletionWarningViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionDeletionWarningViewModel.Factory
            factory.create(args.testRegistrationRequest)
        }
    )
    private val binding: FragmentSubmissionDeletionWarningBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            when (viewModel.getTestType()) {
                BaseCoronaTest.Type.PCR -> {
                    headline.text = getString(R.string.submission_deletion_warning_headline_pcr_test)
                    body.text = getString(R.string.submission_deletion_warning_body_pcr_test)
                }

                BaseCoronaTest.Type.RAPID_ANTIGEN -> {
                    headline.text = getString(R.string.submission_deletion_warning_headline_antigen_test)
                    body.text = getString(R.string.submission_deletion_warning_body_antigen_test)
                }
            }

            continueButton.setOnClickListener { viewModel.deleteExistingAndRegisterNewTest() }

            toolbar.setNavigationOnClickListener { popBackStack() }
        }

        viewModel.registrationState.observe2(this) { state ->
            val isWorking = state is State.Working
            binding.apply {
                submissionQrCodeScanSpinner.isVisible = isWorking
                continueButton.isVisible = !isWorking
            }
            when (state) {
                State.Idle,
                State.Working -> {
                    // Handled above
                }
                is State.Error -> {
                    state.getDialogBuilder(requireContext(), args.testRegistrationRequest is CoronaTestTAN).show()
                    popBackStack()
                }
                is State.TestRegistered -> when {
                    state.test.isPositive -> {
                        if (args.testRegistrationRequest is CoronaTestTAN) {
                            SubmissionDeletionWarningFragmentDirections
                                .actionSubmissionDeletionFragmentToSubmissionTestResultNoConsentFragment(
                                    testIdentifier = state.test.identifier
                                )
                        } else {
                            NavGraphDirections.actionToSubmissionTestResultAvailableFragment(
                                testIdentifier = state.test.identifier
                            )
                        }
                    }
                    else -> NavGraphDirections.actionSubmissionTestResultPendingFragment(
                        testIdentifier = state.test.identifier
                    )
                }.also { findNavController().navigate(it, navOptions) }
            }

            viewModel.routeToScreen.observe2(this) { event ->
                Timber.d("Navigating to %s", event)
                when (event) {
                    DuplicateWarningEvent.Back -> popBackStack()
                    is DuplicateWarningEvent.Direction -> findNavController().navigate(event.direction, navOptions)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
