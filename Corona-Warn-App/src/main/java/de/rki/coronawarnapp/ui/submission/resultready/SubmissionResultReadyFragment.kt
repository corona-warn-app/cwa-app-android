package de.rki.coronawarnapp.ui.submission.resultready

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionResultReadyBinding
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.ui.submission.SubmissionCancelDialog
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The [SubmissionResultReadyFragment] displays information to a user if no consent is given
 */
class SubmissionResultReadyFragment : Fragment(R.layout.fragment_submission_result_ready), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionResultReadyViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionResultReadyBinding by viewBindingLazy()

    private lateinit var uploadDialog: SubmissionBlockingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadDialog = SubmissionBlockingDialog(requireContext())

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onConfirmSkipSymptomsInput()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        setButtonOnClickListener()

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToMainActivity -> doNavigate(
                    SubmissionResultReadyFragmentDirections.actionSubmissionResultReadyFragmentToMainFragment()
                )

                is SubmissionNavigationEvents.NavigateToSymptomIntroduction -> doNavigate(
                    SubmissionResultReadyFragmentDirections
                        .actionSubmissionResultReadyFragmentToSubmissionSymptomIntroductionFragment()
                )
            }
        }
        viewModel.showUploadDialog.observe2(this) {
            uploadDialog.setState(show = it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onNewUserActivity()
        binding.submissionDoneNoConsentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionDoneNoConsentHeader.headerButtonBack.buttonIcon.setOnClickListener {
            onConfirmSkipSymptomsInput()
        }
        binding.submissionDoneButtonContinueWithSymptomRecording.setOnClickListener {
            viewModel.onContinueWithSymptomRecordingPressed()
        }
        binding.submissionDoneContactButtonFinishFlow.setOnClickListener {
            onConfirmSkipSymptomsInput()
        }
    }

    private fun onConfirmSkipSymptomsInput() {
        SubmissionCancelDialog(requireContext()).show {
            viewModel.onSkipSymptomsConfirmed()
        }
    }
}
