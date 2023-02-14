package de.rki.coronawarnapp.ui.settings.start

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * This is the setting overview page.
 */
@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val vm: SettingsFragmentViewModel by viewModels()

    private val binding: FragmentSettingsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.tracingState.observe(viewLifecycleOwner) {
            binding.settingsTracing.configureSettingsRowIcon(
                it.getTracingIcon(requireContext()),
                it.getTracingIconColor(requireContext())
            )
            binding.settingsTracing.configureSettingsRowSubtitle(it.getTracingStatusText(requireContext()))
        }
        vm.notificationSettingsState.observe(viewLifecycleOwner) {
            binding.settingsNotifications.configureSettingsRowIcon(
                it.getNotificationIcon(requireContext()),
                it.getNotificationIconColor(requireContext())
            )
            binding.settingsNotifications.configureSettingsRowSubtitle(it.getNotificationStatusText(requireContext()))
        }
        vm.backgroundPriorityState.observe(viewLifecycleOwner) {
            binding.settingsBackgroundPriority.isVisible = it.showBackgroundPrioritySettings()
            binding.settingsBackgroundPriority.configureSettingsRowIcon(
                it.getBackgroundPriorityIcon(requireContext()),
                it.getBackgroundPriorityIconColor(requireContext())
            )
            binding.settingsBackgroundPriority.configureSettingsRowSubtitle(
                it.getBackgroundPriorityText(requireContext())
            )
        }

        vm.analyticsState.observe(viewLifecycleOwner) {
            binding.settingsPrivacyPreservingAnalytics.configureSettingsRowIcon(
                it.getPrivacyPreservingAnalyticsIcon(requireContext()),
                it.getPrivacyPreservingAnalyticsIconColor(requireContext())
            )
            binding.settingsPrivacyPreservingAnalytics.configureSettingsRowSubtitle(
                it.getPrivacyPreservingAnalyticsText(requireContext())
            )
        }

        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()

        binding.settingsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        val tracingRow = binding.settingsTracing
        val notificationRow = binding.settingsNotifications
        val backgroundPriorityRow = binding.settingsBackgroundPriority
        val privacyPreservingAnalyticsRow = binding.settingsPrivacyPreservingAnalytics
        val resetRow = binding.settingsReset
        resetRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsResetFragment()
            )
        }
        tracingRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsTracingFragment()
            )
        }
        notificationRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsNotificationFragment()
            )
        }
        backgroundPriorityRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsBackgroundPriorityFragment()
            )
        }

        privacyPreservingAnalyticsRow.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsPrivacyPreservingAnalyticsFragment()
            )
        }

        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }
}
