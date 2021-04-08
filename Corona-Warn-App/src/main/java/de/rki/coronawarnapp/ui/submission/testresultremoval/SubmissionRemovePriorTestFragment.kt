package de.rki.coronawarnapp.ui.submission.testresultremoval

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionRemovePriorTestFragment : Fragment(R.layout.fragment_submission_remove_prior_test_result), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: SubmissionRemovePriorTestFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding:  SubmissionRemovePriorTestFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            cancelButton.setOnClickListener {
                //viewModel.onNextButtonClick()
            }

            contactDiaryOnboardingPrivacyInformation.setOnClickListener {
                //viewModel.onPrivacyButtonPress()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
