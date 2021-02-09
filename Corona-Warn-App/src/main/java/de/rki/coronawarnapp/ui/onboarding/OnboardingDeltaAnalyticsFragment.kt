package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingDeltaPpaBinding
import de.rki.coronawarnapp.databinding.FragmentOnboardingPpaBinding
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class OnboardingDeltaAnalyticsFragment : Fragment(R.layout.fragment_onboarding_delta_ppa), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingAnalyticsViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentOnboardingDeltaPpaBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { vm.onNextButtonClick() }
            onboardingButtonDisable.setOnClickListener { vm.onDisableClick() }
            onboardingButtonBack.buttonIcon.setOnClickListener { requireActivity().onBackPressed() }

            federalStateRow.setOnClickListener {
                doNavigate(
                    OnboardingAnalyticsFragmentDirections.actionOnboardingAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.FEDERAL_STATE
                    )
                )
            }
            districtRow.setOnClickListener {
                doNavigate(
                    OnboardingAnalyticsFragmentDirections.actionOnboardingAnalyticsFragmentToAnalyticsUserInputFragment(
                        type = AnalyticsUserInputFragment.InputType.DISTRICT
                    )
                )
            }
            ageGroupRow.setOnClickListener {
                doNavigate(
                    OnboardingAnalyticsFragmentDirections.actionOnboardingAnalyticsFragmentToAnalyticsUserInputFragment(
                        type = AnalyticsUserInputFragment.InputType.AGE_GROUP
                    )
                )
            }
            moreInfoRow.setOnClickListener {
                doNavigate(
                    OnboardingAnalyticsFragmentDirections.actionOnboardingAnalyticsFragmentToPpaMoreInfoFragment()
                )
            }
        }
        vm.completedOnboardingEvent.observe2(this) {
            popBackStack()
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
