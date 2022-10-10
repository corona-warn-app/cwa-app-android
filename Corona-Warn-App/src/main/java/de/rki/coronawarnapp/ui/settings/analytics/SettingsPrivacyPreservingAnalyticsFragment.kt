package de.rki.coronawarnapp.ui.settings.analytics

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsPrivacyPreservingAnalyticsBinding
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SettingsPrivacyPreservingAnalyticsFragment :
    Fragment(R.layout.fragment_settings_privacy_preserving_analytics),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SettingsPrivacyPreservingAnalyticsViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSettingsPrivacyPreservingAnalyticsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            settingsPpaHeader.setNavigationOnClickListener {
                popBackStack()
            }

            settingsPpaSwitchRow.setUserToggleListener { _, _ ->
                viewModel.analyticsToggleEnabled()
            }

            federalStateRow.setOnClickListener {
                findNavController().navigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections
                        .actionSettingsPrivacyPreservingAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.FEDERAL_STATE
                        )
                )
            }

            districtRow.setOnClickListener {
                findNavController().navigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections
                        .actionSettingsPrivacyPreservingAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.DISTRICT
                        )
                )
            }
            ageGroupRow.setOnClickListener {
                findNavController().navigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections
                        .actionSettingsPrivacyPreservingAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.AGE_GROUP
                        )
                )
            }

            moreInfoRow.setOnClickListener {
                findNavController().navigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections
                        .actionSettingsPrivacyPreservingAnalyticsFragmentToPpaMoreInfoFragment()
                )
            }
        }

        viewModel.settingsPrivacyPreservingAnalyticsState.observe2(this) {
            binding.ageGroupRow.apply {
                isGone = !it.isAgeGroupVisible
                setSubtitle(it.getAgeGroupRowBodyText(requireContext()))
            }

            binding.districtRow.apply {
                isGone = !it.isDistrictRowVisible
                setSubtitle(it.getDistrictRowBodyText(requireContext()))
            }

            binding.federalStateRow.apply {
                isGone = !it.isFederalStateRowVisible
                setSubtitle(it.getFederalStateRowBodyText(requireContext()))
            }

            binding.settingsPpaSwitchRow.setChecked(it.isAnalyticsEnabled, notify = false)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingPpaContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
