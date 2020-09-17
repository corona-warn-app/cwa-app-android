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
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel

class OnboardingInteroperabilityFragment : Fragment() {
    private var _binding: FragmentOnboardingInteroperabilityBinding? = null
    private val binding: FragmentOnboardingInteroperabilityBinding get() = _binding!!

    private val interoperabilityViewModel: InteroperabilityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingInteroperabilityBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.interopViewModel = interoperabilityViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        interoperabilityViewModel.saveInteroperabilityUsed()
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {

        binding.onboardingButtonNext.setOnClickListener {
            findNavController().doNavigate(
                OnboardingInteroperabilityFragmentDirections
                    .actionOnboardingInteroperabilityFragmentToOnboardingTestFragment()
            )
        }
    }
}
