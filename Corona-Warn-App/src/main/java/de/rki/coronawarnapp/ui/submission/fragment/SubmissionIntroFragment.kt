package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionIntroBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionIntroViewModel
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The [SubmissionIntroFragment] displays information about how the corona warning system works
 */
class SubmissionIntroFragment : Fragment(R.layout.fragment_submission_intro), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionIntroViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionIntroBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToMainActivity ->
                    findNavController().doNavigate(
                        SubmissionIntroFragmentDirections.actionSubmissionIntroFragmentToMainFragment()
                    )
                is SubmissionNavigationEvents.NavigateToDispatcher ->
                    findNavController().doNavigate(
                        SubmissionIntroFragmentDirections.actionSubmissionIntroFragmentToSubmissionDispatcherFragment()
                    )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionIntroRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionIntroHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackPressed()
        }
        binding.submissionIntroButtonNext.setOnClickListener {
            viewModel.onNextPressed()
        }
    }
}
