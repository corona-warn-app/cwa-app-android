package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.NavGraph
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDispatcherBinding
import de.rki.coronawarnapp.qrcode.ui.QrCodeScannerFragmentArgs
import de.rki.coronawarnapp.srs.ui.consent.SrsSubmissionConsentFragmentArgs
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDispatcherViewModel
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
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
                    findNavController().navigate(
                        SubmissionDispatcherFragmentDirections
                            .actionSubmissionDispatcherFragmentToSubmissionTanFragment(
                                comesFromDispatcherFragment = true
                            )
                    )
                is SubmissionNavigationEvents.OpenTestCenterUrl ->
                    openUrl(getString(R.string.submission_dispatcher_card_test_center_link))
                is SubmissionNavigationEvents.NavigateToContact ->
                    findNavController().navigate(
                        SubmissionDispatcherFragmentDirections
                            .actionSubmissionDispatcherFragmentToSubmissionContactFragment()
                    )
                is SubmissionNavigationEvents.NavigateToQRCodeScan -> openUniversalScanner()

                is SubmissionNavigationEvents.NavigateToProfileList -> {
                    val profileGraph = findNavController().graph.findNode(R.id.rapid_test_profile_nav_graph) as NavGraph
                    val startDestination =
                        if (it.onboarded) R.id.profileListFragment else R.id.profileOnboardingFragment
                    profileGraph.setStartDestination(startDestination)

                    findNavController().navigate(
                        SubmissionDispatcherFragmentDirections
                            .actionSubmissionDispatcherFragmentToRapidTestProfileNavGraph()
                    )
                }
                is SubmissionNavigationEvents.NavigateToSelfTestConsentScreen -> {
                    findNavController().navigate(
                        R.id.srs_nav_graph,
                        SrsSubmissionConsentFragmentArgs(it.positiveNoAnswer).toBundle()
                    )
                }

                else -> Unit
            }
        }

        viewModel.srsError.observe2(this) {
            displayDialog {
                setError(it)
                positiveButton(R.string.nm_faq_label) { openUrl(R.string.srs_faq_url) }
                negativeButton(android.R.string.ok)
            }
        }
    }

    private fun openUniversalScanner() {
        val dispatcherCard = binding.submissionDispatcherQr.apply {
            transitionName = "shared_element_container"
        }
        val args = QrCodeScannerFragmentArgs(comesFromDispatcherFragment = true).toBundle()
        findNavController().navigate(
            R.id.action_to_universal_scanner,
            args,
            null,
            FragmentNavigatorExtras(dispatcherCard to dispatcherCard.transitionName)
        )
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDispatcherRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setButtonOnClickListener() {
        binding.apply {
            toolbar.setNavigationOnClickListener { viewModel.onBackPressed() }
            srsSelfTest.setOnClickListener { viewModel.onSrsTileClicked() }
            positiveSelfTest.setOnClickListener { viewModel.onSrsTileClicked(positiveNoAnswer = true) }
            submissionDispatcherQr.setOnClickListener { viewModel.onQRCodePressed() }
            submissionDispatcherTanCode.setOnClickListener { viewModel.onTanPressed() }
            submissionDispatcherTanTele.setOnClickListener { viewModel.onTeleTanPressed() }
            submissionDispatcherTestCenter.setOnClickListener { viewModel.onTestCenterPressed() }
            profileCard.setOnClickListener { viewModel.onProfilePressed() }
        }
    }
}
