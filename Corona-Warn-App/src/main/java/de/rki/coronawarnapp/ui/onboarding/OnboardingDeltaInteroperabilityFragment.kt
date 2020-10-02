package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingDeltaInteroperabilityBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class OnboardingDeltaInteroperabilityFragment :
    Fragment(R.layout.fragment_onboarding_delta_interoperability), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingDeltaInteroperabilityFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentOnboardingDeltaInteroperabilityBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.countryList.observe2(this) {
            binding.countryData = it
        }
        vm.saveInteroperabilityUsed()

        binding.onboardingInclude.onboardingDeltaExpandedTermsTextLink.convertToHyperlink(getString(R.string.information_terms_html_path))
        binding.onboardingInclude.onboardingDeltaExpandedTermsTextLink
            .movementMethod = LinkMovementMethod.getInstance()

        binding.onboardingInclude.onboardingDeltaExpandedTermsTextLink.setOnClickListener {
            findNavController().doNavigate(OnboardingDeltaInteroperabilityFragmentDirections.actionOnboardingDeltaInteroperabilityFragmentToInformationTermsFragment())
        }

        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            vm.onBackPressed()
        }
        binding.onboardingButtonNext.setOnClickListener {
            vm.onBackPressed()
        }

        vm.navigateBack.observe2(this) {
            if (it) {
                (requireActivity() as MainActivity).goBack()
            }
        }
    }
}
