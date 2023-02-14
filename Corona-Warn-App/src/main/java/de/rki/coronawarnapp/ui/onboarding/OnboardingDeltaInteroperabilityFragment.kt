package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingDeltaInteroperabilityBinding
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class OnboardingDeltaInteroperabilityFragment : Fragment(R.layout.fragment_onboarding_delta_interoperability) {

    private val vm: OnboardingDeltaInteroperabilityFragmentViewModel by viewModels()

    private val binding: FragmentOnboardingDeltaInteroperabilityBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.countryList.observe2(this) {
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

        vm.navigateBack.observe2(this) {
            if (it) {
                popBackStack()
            }
        }
    }
}
