package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingDeltaInteroperabilityBinding
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class OnboardingDeltaInteroperabilityFragment :
    Fragment(R.layout.fragment_onboarding_delta_interoperability), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingDeltaInteroperabilityFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentOnboardingDeltaInteroperabilityBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.countryList.observe(viewLifecycleOwner) {
            binding.deltaInteroperabilityCountryList.setCountryList(it)
            if (it.isEmpty()) {
                binding.countryListGroup.isVisible = false
                binding.noCountriesGroup.isVisible = true
            }
        }
        vm.saveInteroperabilityUsed()

        binding.onboardingDeltaExpandedTermsTextLink
            .convertToHyperlink(getString(R.string.information_terms_html_path))
        binding.onboardingDeltaExpandedTermsTextLink
            .movementMethod = LinkMovementMethod.getInstance()

        binding.onboardingDeltaExpandedTermsTextLink.setOnClickListener {
            findNavController().navigate(
                OnboardingDeltaInteroperabilityFragmentDirections
                    .actionOnboardingDeltaInteroperabilityFragmentToInformationTermsFragment()
            )
        }

        binding.onboardingDeltaToolbar.setNavigationOnClickListener { vm.onBackPressed() }
        binding.onboardingButtonNext.setOnClickListener { vm.onBackPressed() }

        vm.navigateBack.observe(viewLifecycleOwner) {
            if (it) {
                popBackStack()
            }
        }
    }
}
