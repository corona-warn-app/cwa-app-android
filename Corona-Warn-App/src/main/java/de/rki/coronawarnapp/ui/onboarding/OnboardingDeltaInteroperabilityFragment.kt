package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingDeltaInteroperabilityBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class OnboardingDeltaInteroperabilityFragment :
    Fragment(R.layout.fragment_onboarding_delta_interoperability), View.OnClickListener {

    private val binding: FragmentOnboardingDeltaInteroperabilityBinding by viewBindingLazy()
    private val interoperabilityViewModel: InteroperabilityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.interoperabilityViewModel = interoperabilityViewModel
        interoperabilityViewModel.saveInteroperabilityUsed()

        binding.onboardingButtonNext.setOnClickListener(this)
        binding.onboardingButtonBack.buttonIcon.setOnClickListener(this)
    }

    private fun navigateBack() {
        (activity as? MainActivity)?.goBack()
    }

    override fun onClick(view: View?) {
        navigateBack()
    }
}
