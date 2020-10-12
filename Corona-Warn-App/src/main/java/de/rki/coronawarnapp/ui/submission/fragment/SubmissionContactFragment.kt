package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionContactBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionContactViewModel
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The [SubmissionContactFragment] allows requesting a teletan via phone
 */
class SubmissionContactFragment : Fragment(R.layout.fragment_submission_contact), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionContactViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionContactBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        viewModel.navigateBack.observe2(this) {
            findNavController().popBackStack()
        }

        viewModel.dial.observe2(this) {
            dial()
        }

        viewModel.navigateToTan.observe2(this) {
            findNavController().doNavigate(
                SubmissionContactFragmentDirections.actionSubmissionContactFragmentToSubmissionTanFragment()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionContactRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionContactHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackPressed()
        }
        binding.submissionContactButtonCall.setOnClickListener {
            viewModel.onDialPressed()
        }
        binding.includeSubmissionContact.submissionContactStep1Number.setOnClickListener {
            viewModel.onDialPressed()
        }
        binding.submissionContactButtonEnter.setOnClickListener {
            viewModel.onEnterTanPressed()
        }
    }

    private fun dial() = context?.let {
        val number = getString(R.string.submission_contact_number_dial)
        ExternalActionHelper.call(this, number)
    }
}
