package de.rki.coronawarnapp.ui.settings.backgroundpriority

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsBackgroundPriorityBinding
import de.rki.coronawarnapp.ui.base.startActivitySafely
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * This is the setting background priority page. Here the user sees the background priority setting status.
 * If background priority is disabled it can be activated.
 */
@AndroidEntryPoint
class SettingsBackgroundPriorityFragment : Fragment(R.layout.fragment_settings_background_priority) {

    private val vm: SettingsBackgroundPriorityFragmentViewModel by viewModels()

    private val binding: FragmentSettingsBackgroundPriorityBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.backgroundPriorityState.observe(viewLifecycleOwner) {
            binding.settingsBackgroundPriorityDetails.getInformationImageAndDescription(
                it.getHeaderIllustration(requireContext()),
                it.getHeaderIllustrationDescription(requireContext())
            )
            binding.settingsRowBackgroundPriorityHeaderBody.text = it.getButtonStateLabel(requireContext())
        }

        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.settingsBackgroundPriorityContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setButtonOnClickListener() {
        val settingsRow = binding.settingsRowBackgroundPriority

        // enable background priority
        settingsRow.setOnClickListener {
            (requireActivity() as MainActivity).apply {
                startActivitySafely(powerManagement.toBatteryOptimizationSettingsIntent)
            }
        }

        // explanatory card
        binding.settingsTracingStatusConnection.setOnClickListener {
            (requireActivity() as MainActivity).apply {
                startActivity(powerManagement.toBatteryOptimizationSettingsIntent)
            }
        }

        // back navigation
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }
}
