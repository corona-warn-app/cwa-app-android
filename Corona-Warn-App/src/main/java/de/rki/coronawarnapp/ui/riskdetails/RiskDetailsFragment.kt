package de.rki.coronawarnapp.ui.riskdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.databinding.FragmentRiskDetailsBinding
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel

/**
 * This is the detail view of the risk card if additional information for the user.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 */
class RiskDetailsFragment : BaseFragment() {

    companion object {
        private val TAG: String? = RiskDetailsFragment::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentRiskDetailsBinding? = null
    private val binding: FragmentRiskDetailsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRiskDetailsBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        tracingViewModel.refreshRiskLevel()
        tracingViewModel.refreshExposureSummary()
        tracingViewModel.refreshLastTimeDiagnosisKeysFetchedDate()
        settingsViewModel.refreshBackgroundJobEnabled()
        TimerHelper.checkManualKeyRetrievalTimer()
    }

    private fun setButtonOnClickListeners() {
        binding.riskDetailsButtonUpdate.setOnClickListener {
            tracingViewModel.refreshRiskLevel()
            tracingViewModel.refreshDiagnosisKeys()
            TimerHelper.startManualKeyRetrievalTimer()
        }
        binding.riskDetailsButtonEnableTracing.setOnClickListener {
            doNavigate(
                RiskDetailsFragmentDirections.actionRiskDetailsFragmentToSettingsTracingFragment()
            )
        }
        binding.riskDetailsRiskCard.riskCardHeader.riskCardHeaderButtonBack.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
