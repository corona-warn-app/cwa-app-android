package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDoneBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDoneViewModel
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The [SubmissionDoneFragment] displays information to a user that submitted his exposure keys
 */
class SubmissionDoneFragment : Fragment(R.layout.fragment_submission_done), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionDoneViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionDoneBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        viewModel.navigateBack.observe2(this) {
            findNavController().doNavigate(
                SubmissionDoneFragmentDirections.actionSubmissionDoneFragmentToMainFragment()
            )
        }

        viewModel.navigateToMain.observe2(this) {
            findNavController().doNavigate(
                SubmissionDoneFragmentDirections.actionSubmissionDoneFragmentToMainFragment()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDoneContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionDoneHeader.headerButtonBack.buttonIcon.setOnClickListener {

            viewModel.onBackPressed()
        }
        binding.submissionDoneButtonDone.setOnClickListener {

            viewModel.onDonePressed()
        }
    }
}
