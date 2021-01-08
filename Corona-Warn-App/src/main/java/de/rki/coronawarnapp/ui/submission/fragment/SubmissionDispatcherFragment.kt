package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDispatcherBinding
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDispatcherViewModel
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionDispatcherFragment : Fragment(R.layout.fragment_submission_dispatcher), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionDispatcherViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionDispatcherBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToMainActivity ->
                    findNavController().popBackStack()
                is SubmissionNavigationEvents.NavigateToTAN ->
                    doNavigate(
                        SubmissionDispatcherFragmentDirections
                            .actionSubmissionDispatcherFragmentToSubmissionTanFragment()
                    )
                is SubmissionNavigationEvents.NavigateToContact ->
                    doNavigate(
                        SubmissionDispatcherFragmentDirections
                            .actionSubmissionDispatcherFragmentToSubmissionContactFragment()
                    )
                is SubmissionNavigationEvents.NavigateToConsent ->
                    doNavigate(
                        SubmissionDispatcherFragmentDirections
                            .actionSubmissionDispatcherFragmentToSubmissionConsentFragment()
                    )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDispatcherRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionDispatcherHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackPressed()
        }
        binding.submissionDispatcherQr.dispatcherCard.setOnClickListener {
            viewModel.onQRCodePressed()
        }
        binding.submissionDispatcherTanCode.dispatcherCard.setOnClickListener {
            viewModel.onTanPressed()
        }
        binding.submissionDispatcherTanTele.dispatcherCard.setOnClickListener {
            viewModel.onTeleTanPressed()
        }
    }
}
