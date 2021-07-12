package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDispatcherBinding
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDispatcherViewModel
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.findNestedGraph
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class SubmissionDispatcherFragment : Fragment(R.layout.fragment_submission_dispatcher), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionDispatcherViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionDispatcherBinding by viewBinding()

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
                is SubmissionNavigationEvents.OpenTestCenterUrl -> openUrl(getString(R.string.submission_dispatcher_card_test_center_link))
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
                is SubmissionNavigationEvents.NavigateToCreateProfile -> {
                    val ratGraph = findNavController().graph.findNode(R.id.rapid_test_profile_nav_graph) as NavGraph
                    ratGraph.startDestination = if (it.onboarded)
                        R.id.ratProfileCreateFragment
                    else R.id.ratProfileOnboardingFragment

                    doNavigate(
                        SubmissionDispatcherFragmentDirections
                            .actionSubmissionDispatcherFragmentToRapidTestProfileNavGraph()
                    )
                }
                is SubmissionNavigationEvents.NavigateToOpenProfile -> {
                    findNestedGraph(R.id.rapid_test_profile_nav_graph).startDestination = R.id.ratProfileQrCodeFragment
                    doNavigate(
                        SubmissionDispatcherFragmentDirections
                            .actionSubmissionDispatcherFragmentToRapidTestProfileNavGraph()
                    )
                }
            }
        }

        viewModel.profileCardId.observe(viewLifecycleOwner) { layoutId ->
            binding.ratProfileCard.viewStub?.apply {
                layoutResource = layoutId
                Timber.d("layoutId=$layoutId")
                inflate()
                binding.ratProfileCard.root.setOnClickListener {
                    viewModel.onClickProfileCard()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDispatcherRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
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
        binding.submissionDispatcherTestCenter.dispatcherCard.setOnClickListener {
            viewModel.onTestCenterPressed()
        }
    }
}
