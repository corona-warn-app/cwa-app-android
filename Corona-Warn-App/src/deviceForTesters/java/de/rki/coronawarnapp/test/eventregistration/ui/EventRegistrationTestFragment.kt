package de.rki.coronawarnapp.test.eventregistration.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestEventregistrationBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class EventRegistrationTestFragment : Fragment(R.layout.fragment_test_eventregistration), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: EventRegistrationTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestEventregistrationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            scanCheckInQrCode.setOnClickListener {
                doNavigate(
                    EventRegistrationTestFragmentDirections
                        .actionEventRegistrationTestFragmentToScanCheckInQrCodeFragment()
                )
            }

            testQrCodeCreation.setOnClickListener {
                doNavigate(
                    EventRegistrationTestFragmentDirections
                        .actionEventRegistrationTestFragmentToTestQrCodeCreationFragment()
                )
            }

            createEventButton.setOnClickListener {
                findNavController().navigate(R.id.createEventTestFragment)
            }

            showEventsButton.setOnClickListener {
                findNavController().navigate(R.id.showStoredEventsTestFragment)
            }

            showQrCodeDetailFragment.setOnClickListener {
                findNavController().navigate(R.id.qrCodeDetailFragment)
            }

            startCreateEventFlowButton.setOnClickListener {
                findNavController().navigate(R.id.traceLocationOrganizerCategoriesFragment)
            }

            showOrganizerList.setOnClickListener {
                findNavController().navigate(R.id.traceLocationOrganizerListFragment)
            }

            generateTestTraceLocations.setOnClickListener {
                viewModel.generateTestTraceLocations()
            }
        }
        binding.runMatcher.setOnClickListener {
            viewModel.runMatcher()
        }

        binding.downloadReportedCheckIns.setOnClickListener {
            viewModel.downloadWarningPackages()
        }

        binding.calculateRisk.setOnClickListener {
            viewModel.runRiskCalculationPerCheckInDay()
        }

        viewModel.checkInOverlapsText.observe2(this) {
            binding.matchingResultText.text = it
        }

        viewModel.checkInRiskPerDayText.observe2(this) {
            binding.riskCalculationResultText.text = it
        }

        viewModel.matchingRuntime.observe2(this) {
            binding.matchingRuntimeText.text = "Matching runtime in millis: $it"
        }

        viewModel.riskCalculationRuntime.observe2(this) {
            binding.riskCalculationRuntimeText.text = "Risk calculation runtime in millis: $it"
        }

        binding.runPTWarningTask.setOnClickListener {
            viewModel.runWarningPackageTask()
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Event Registration",
            description = "View & Control the event registration.",
            targetId = R.id.eventRegistrationTestFragment
        )
    }
}
