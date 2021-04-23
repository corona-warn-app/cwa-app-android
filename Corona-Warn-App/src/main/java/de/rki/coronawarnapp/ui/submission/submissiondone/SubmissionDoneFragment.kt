package de.rki.coronawarnapp.ui.submission.submissiondone

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDoneBinding
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionDoneFragment : Fragment(R.layout.fragment_submission_done), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: SubmissionDoneViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionDoneBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            toolbar.apply {
                navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                navigationContentDescription = getString(R.string.accessibility_close)
                setNavigationOnClickListener { viewModel.onFinishButtonClick() }
            }

            submissionDoneButtonDone.setOnClickListener {
                viewModel.onFinishButtonClick()
            }
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                SubmissionNavigationEvents.NavigateToMainActivity -> {
                    doNavigate(
                      SubmissionDoneFragmentDirections.actionSubmissionDoneFragmentToMainFragment()
                    )
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        binding.submissionDoneContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
