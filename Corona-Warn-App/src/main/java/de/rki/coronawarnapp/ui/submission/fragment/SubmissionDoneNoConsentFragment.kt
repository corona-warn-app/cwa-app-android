package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDoneNoConsentBinding
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDoneNoConsentViewModel
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The [SubmissionDoneNoConsentFragment] displays information to a user if no consent is given
 */
class SubmissionDoneNoConsentFragment : Fragment(R.layout.fragment_submission_done_no_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionDoneNoConsentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionDoneNoConsentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToMainActivity ->
                    doNavigate(
                        SubmissionDoneNoConsentFragmentDirections
                            .actionSubmissionDoneNoConsentFragmentToMainFragment()
                    )

                SubmissionNavigationEvents.NavigateToSymptomIntroduction ->
                    doNavigate(
                        SubmissionDoneNoConsentFragmentDirections
                            .actionSubmissionDoneNoConsentFragmentToSubmissionSymptomIntroductionFragment()
                    )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDoneNoConsentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionDoneNoConsentHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackPressed()
        }
        binding.submissionDoneButtonContinueWithSymptomRecording.setOnClickListener {
            viewModel.onContinueWithSymptomRecordingPressed()
        }
        binding.submissionDoneContactButtonBreak.setOnClickListener {
            viewModel.onBreakFlowPressed()
        }
    }
}
