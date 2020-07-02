package de.rki.coronawarnapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.databinding.FragmentSettingsBackgroundPriorityBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.ExternalActionHelper

/**
 * This is the setting background priority page. Here the user sees the background priority setting status.
 * If background priority is disabled it can be activated.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 */
class SettingsBackgroundPriorityFragment : Fragment() {
    companion object {
        private val TAG: String? = SettingsBackgroundPriorityFragment::class.simpleName
    }

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentSettingsBackgroundPriorityBinding? = null
    private val binding: FragmentSettingsBackgroundPriorityBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBackgroundPriorityBinding.inflate(inflater)
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
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.settingsBackgroundPriorityContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        // refresh required data
        settingsViewModel.refreshBackgroundPriorityEnabled(requireContext())
    }

    private fun setButtonOnClickListener() {
        val switch = binding.settingsSwitchRowBackgroundPriority.settingsSwitchRowSwitch
        val switchRow = binding.settingsSwitchRowBackgroundPriority.settingsSwitchRow

        // enable background priority
        setOf(switch, switchRow).forEach {
            it.setOnClickListener {
                val isPriorityEnabled = settingsViewModel.isBackgroundPriorityEnabled.value == true

                if (!isPriorityEnabled)
                    ExternalActionHelper.disableBatteryOptimizations(requireContext())
            }
        }

        // explanatory card
        binding.settingsTracingStatusConnection.tracingStatusCardButton.setOnClickListener {
            ExternalActionHelper.toBatteryOptimizationSettings(requireContext())
        }

        // back navigation
        binding.settingsBackgroundPriorityHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
