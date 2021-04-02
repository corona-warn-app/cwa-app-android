package de.rki.coronawarnapp.test.eventregistration.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestEventregistrationBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
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

        binding.runMatcher.setOnClickListener {
            viewModel.runMatcher()
        }

        binding.downloadReportedCheckIns.setOnClickListener {
            Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
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
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Event Registration",
            description = "View & Control the event registration.",
            targetId = R.id.eventRegistrationTestFragment
        )
    }
}
