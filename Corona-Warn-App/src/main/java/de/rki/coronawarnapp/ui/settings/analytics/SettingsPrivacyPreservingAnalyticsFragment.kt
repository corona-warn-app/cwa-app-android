package de.rki.coronawarnapp.ui.settings.analytics

import de.rki.coronawarnapp.databinding.FragmentSettingsPrivacyPreservingAnalyticsBinding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SettingsPrivacyPreservingAnalyticsFragment : Fragment(R.layout.fragment_settings_privacy_preserving_analytics), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsPrivacyPreservingAnalyticsViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSettingsPrivacyPreservingAnalyticsBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            settingsPpaHeader.headerButtonBack.buttonIcon.setOnClickListener {
                (activity as MainActivity).goBack()
            }

            settingsPpaSwitchRow.settingsSwitchRowSwitch.setOnCheckedChangeListener { view, _ ->
                // Make sure that listener is called by user interaction
                if (!view.isPressed) return@setOnCheckedChangeListener

                vm.analyticsToggleEnabled()
            }

            federalStateRow.setOnClickListener {
                doNavigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections.actionSettingsPrivacyPreservingAnalyticsFragmentToAnalyticsUserInputFragment(
                        type = AnalyticsUserInputFragment.InputType.FEDERAL_STATE
                    )
                )
            }

            districtRow.setOnClickListener {
                doNavigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections.actionSettingsPrivacyPreservingAnalyticsFragmentToAnalyticsUserInputFragment(
                        type = AnalyticsUserInputFragment.InputType.DISTRICT
                    )
                )
            }
            ageGroupRow.setOnClickListener {
                doNavigate(
                    SettingsPrivacyPreservingAnalyticsFragmentDirections.actionSettingsPrivacyPreservingAnalyticsFragmentToAnalyticsUserInputFragment(
                        type = AnalyticsUserInputFragment.InputType.AGE_GROUP
                    )
                )
            }
        }

        vm.ageGroup.observe2(this) {
            binding.ageGroupRowBody.text = getString(it.labelStringRes)
        }

        vm.federalState.observe2(this) {
            binding.districtRow.visibility = if (it != PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.federalStateRowBody.text = getString(it.labelStringRes)
        }
        vm.district.observe2(this) {
            binding.districtRowBody.text = it?.districtName
                ?: getString(R.string.analytics_userinput_district_unspecified)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingPpaContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
