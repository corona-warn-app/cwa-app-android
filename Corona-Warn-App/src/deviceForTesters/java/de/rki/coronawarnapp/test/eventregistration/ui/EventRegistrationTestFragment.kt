package de.rki.coronawarnapp.test.eventregistration.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestEventregistrationBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
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

            startCreateEventFlowButton.setOnClickListener {
                findNavController().navigate(R.id.traceLocationOrganizerCategoriesFragment)
            }
        }
        binding.runMatcher.setOnClickListener {
            Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.downloadReportedCheckIns.setOnClickListener {
            viewModel.downloadWarningPackages()
        }

        binding.calculateRisk.setOnClickListener {
            Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
        }

        viewModel.checkInOverlaps.observe2(this) {
            val text = it.fold(StringBuilder()) { stringBuilder, checkInOverlap ->
                stringBuilder
                    .append("CheckIn Id ${checkInOverlap.checkInId}")
                    .append("Date ${checkInOverlap.localDate}")
                    .append("Min. ${checkInOverlap.overlap.standardMinutes}")
                    .append("\n")
            }
            binding.resultText.text = text
            binding.resultText.visibility = View.VISIBLE
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
