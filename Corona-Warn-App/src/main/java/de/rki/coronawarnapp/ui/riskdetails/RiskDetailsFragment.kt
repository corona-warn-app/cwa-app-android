package de.rki.coronawarnapp.ui.riskdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentRiskDetailsBinding
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewLifecycle
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.convertToHyperlink

/**
 * This is the detail view of the risk card if additional information for the user.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 */
class RiskDetailsFragment : Fragment() {

    companion object {
        private val TAG: String? = RiskDetailsFragment::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var binding: FragmentRiskDetailsBinding by viewLifecycle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRiskDetailsBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListeners()
        setUpWebLinks()
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        tracingViewModel.refreshRiskLevel()
        tracingViewModel.refreshExposureSummary()
        tracingViewModel.refreshLastTimeDiagnosisKeysFetchedDate()
        TimerHelper.checkManualKeyRetrievalTimer()
        tracingViewModel.refreshActiveTracingDaysInRetentionPeriod()
        binding.riskDetailsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    /**
     * Make the links clickable and convert to hyperlink
     */
    private fun setUpWebLinks() {
        binding.riskDetailsInformationLowriskBodyUrl
            .convertToHyperlink(getString(R.string.onboarding_tracing_easy_language_explanation))
    }

    private fun setButtonOnClickListeners() {
        binding.riskDetailsHeaderButtonBack.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.riskDetailsButtonUpdate.setOnClickListener {
            tracingViewModel.refreshDiagnosisKeys()
            settingsViewModel.updateManualKeyRetrievalEnabled(false)
        }
        binding.riskDetailsButtonEnableTracing.setOnClickListener {
            findNavController().doNavigate(
                RiskDetailsFragmentDirections.actionRiskDetailsFragmentToSettingsTracingFragment()
            )
        }
    }
}
