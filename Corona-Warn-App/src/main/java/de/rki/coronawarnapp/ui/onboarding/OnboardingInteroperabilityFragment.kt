package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentOnboardingInteroperabilityBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.interoperability.InteroperabilityUIHelper
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel

class OnboardingInteroperabilityFragment : Fragment() {
    private var _binding: FragmentOnboardingInteroperabilityBinding? = null
    private val binding: FragmentOnboardingInteroperabilityBinding get() = _binding!!

    private val interoperabilityViewModel: InteroperabilityViewModel by activityViewModels()

    private var uiHelper: InteroperabilityUIHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingInteroperabilityBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.interopViewModel = interoperabilityViewModel
        uiHelper = InteroperabilityUIHelper(interoperabilityViewModel, requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        interoperabilityViewModel.saveInteroperabilityUsed()
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {

        interoperabilityViewModel.refreshInteroperability()

        binding.onboardingInclude.allCountrySwitch.setOnCheckedChangeListener { view, checked ->
            uiHelper?.handleAllCountrySwitchChanged(checked, false)
        }

        binding.onboardingInclude.countryList.onCountrySelectionChanged =
            { _, countryCode, selected ->
                uiHelper?.handleCountrySelected(countryCode, selected, false)
            }

        binding.onboardingButtonNext.setOnClickListener {
            findNavController().doNavigate(
                OnboardingInteroperabilityFragmentDirections.actionOnboardingInteroperabilityFragmentToOnboardingTestFragment()
            )
        }

        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            (activity as OnboardingActivity).goBack()
        }
    }
}
