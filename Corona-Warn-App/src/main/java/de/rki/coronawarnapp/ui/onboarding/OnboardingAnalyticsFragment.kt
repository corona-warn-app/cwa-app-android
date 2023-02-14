package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingPpaBinding
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class OnboardingAnalyticsFragment : Fragment(R.layout.fragment_onboarding_ppa), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: OnboardingAnalyticsViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentOnboardingPpaBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { viewModel.onProceed(true) }
            onboardingButtonDisable.setOnClickListener { viewModel.onProceed(false) }
            toolbar.setNavigationOnClickListener { popBackStack() }

            federalStateRow.setOnClickListener {
                findNavController().navigate(
                    OnboardingAnalyticsFragmentDirections.actionOnboardingAnalyticsFragmentToAnalyticsUserInputFragment(
                        type = AnalyticsUserInputFragment.InputType.FEDERAL_STATE
                    )
                )
            }
            districtRow.setOnClickListener {
                findNavController().navigate(
                    OnboardingAnalyticsFragmentDirections.actionOnboardingAnalyticsFragmentToAnalyticsUserInputFragment(
                        type = AnalyticsUserInputFragment.InputType.DISTRICT
                    )
                )
            }
            ageGroupRow.setOnClickListener {
                findNavController().navigate(
                    OnboardingAnalyticsFragmentDirections.actionOnboardingAnalyticsFragmentToAnalyticsUserInputFragment(
                        type = AnalyticsUserInputFragment.InputType.AGE_GROUP
                    )
                )
            }
            privacyInformation.setOnClickListener {
                findNavController().navigate(
                    OnboardingAnalyticsFragmentDirections.actionOnboardingAnalyticsFragmentToPpaMoreInfoFragment()
                )
            }
        }
        viewModel.completedOnboardingEvent.observe(viewLifecycleOwner) {
            (requireActivity() as OnboardingActivity).completeOnboarding()
        }
        viewModel.ageGroup.observe(viewLifecycleOwner) {
            binding.ageGroupRowBody.text = getString(it.labelStringRes)
            binding.ageGroupRow.contentDescription =
                getString(R.string.onboarding_ppa_age_title) + getString(it.labelStringRes)
        }
        viewModel.federalState.observe(viewLifecycleOwner) {
            binding.districtRow.visibility = if (it != PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.federalStateRowBody.text = getString(it.labelStringRes)
            binding.federalStateRow.contentDescription =
                getString(R.string.onboarding_ppa_state_title) + getString(it.labelStringRes)
        }
        viewModel.district.observe(viewLifecycleOwner) {
            binding.districtRowBody.text = it?.districtName
                ?: getString(R.string.analytics_userinput_district_unspecified)
            binding.districtRow.contentDescription =
                getString(R.string.onboarding_ppa_district_title) + binding.districtRowBody.text
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingPpaContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
