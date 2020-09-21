package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.databinding.FragmentOnboardingDeltaInteroperabilityBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel

class OnboardingDeltaInteroperabilityFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentOnboardingDeltaInteroperabilityBinding? = null
    private val binding: FragmentOnboardingDeltaInteroperabilityBinding get() = _binding!!

    private val interoperabilityViewModel: InteroperabilityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingDeltaInteroperabilityBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.interopViewModel = interoperabilityViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        interoperabilityViewModel.saveInteroperabilityUsed()

        binding.onboardingButtonNext.setOnClickListener(this)
        binding.onboardingButtonBack.buttonIcon.setOnClickListener(this)
    }

    private fun navigateBack() {
        (activity as MainActivity).goBack()
    }

    override fun onClick(view: View?) {
        navigateBack()
    }
}
