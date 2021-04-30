package de.rki.coronawarnapp.ui.settings.analytics

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsPrivacyPreservingAnalyticsBinding
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SettingsPrivacyPreservingAnalyticsFragment :
    Fragment(R.layout.fragment_settings_privacy_preserving_analytics),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SettingsPrivacyPreservingAnalyticsViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSettingsPrivacyPreservingAnalyticsBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            settingsPpaHeader.setNavigationOnClickListener {
                popBackStack()
            }

            settingsPpaSwitchRow.setOnClickListener {
                viewModel.analyticsToggleEnabled()
            }

            federalStateRow.setOnClickListener {
                doNavigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections
                        .actionSettingsPrivacyPreservingAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.FEDERAL_STATE
                        )
                )
            }

            districtRow.setOnClickListener {
                doNavigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections
                        .actionSettingsPrivacyPreservingAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.DISTRICT
                        )
                )
            }
            ageGroupRow.setOnClickListener {
                doNavigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections
                        .actionSettingsPrivacyPreservingAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.AGE_GROUP
                        )
                )
            }

            moreInfoRow.setOnClickListener {
                doNavigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections
                        .actionSettingsPrivacyPreservingAnalyticsFragmentToPpaMoreInfoFragment()
                )
            }
        }

        viewModel.settingsPrivacyPreservingAnalyticsState.observe2(this) {
            binding.ageGroupRowBody.text = it.getAgeGroupRowBodyText(requireContext())

            binding.districtRow.isVisible = it.isDistrictRowVisible()
            binding.districtRowDivider.isVisible = it.isDistrictRowVisible()
            binding.districtRowBody.text = it.getDistrictRowBodyText(requireContext())

            binding.federalStateRowBody.text = it.getFederalStateRowBodyText(requireContext())

            binding.settingsPpaSwitchRow.setChecked(it.isSettingsPpaSwitchOn())
            binding.settingsPpaSwitchRow.setSubtitle(it.getSettingsPpaSwitchRowStateText(requireContext()))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingPpaContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
