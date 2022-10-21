package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionContactBinding
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionContactViewModel
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ExternalActionHelper.callPhone
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.linkifyPhoneNumbers
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The [SubmissionContactFragment] allows requesting a teletan via phone
 */
class SubmissionContactFragment : Fragment(R.layout.fragment_submission_contact), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionContactViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionContactBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        binding.includeSubmissionContact.submissionContactBodyOther.linkifyPhoneNumbers()

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToDispatcher ->
                    findNavController().popBackStack()
                is SubmissionNavigationEvents.NavigateToTAN ->
                    findNavController().navigate(
                        SubmissionContactFragmentDirections.actionSubmissionContactFragmentToSubmissionTanFragment()
                    )
                else -> Unit
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionContactRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.toolbar.setNavigationOnClickListener { viewModel.onBackPressed() }
        binding.includeSubmissionContact.submissionContactNavigationRowPhone.navigationRow
            .setOnClickListener {
                callPhone(getString(R.string.submission_contact_number_display))
            }
        binding.includeSubmissionContact.submissionContactNavigationRowInternationalPhone.navigationRow
            .setOnClickListener {
                callPhone(getString(R.string.submission_contact_button_international_phone))
            }
        binding.submissionContactButtonEnter.setOnClickListener {
            viewModel.onEnterTanPressed()
        }
    }
}
