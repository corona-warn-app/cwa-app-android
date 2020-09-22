package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingDeltaInteroperabilityBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class OnboardingDeltaInteroperabilityFragment :
    Fragment(R.layout.fragment_onboarding_delta_interoperability) {

    private val binding: FragmentOnboardingDeltaInteroperabilityBinding by viewBindingLazy()
    private val interoperabilityViewModel: InteroperabilityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.interoperabilityViewModel = interoperabilityViewModel
        interoperabilityViewModel.saveInteroperabilityUsed()

        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            interoperabilityViewModel.onBackPressed()
        }

        interoperabilityViewModel.navigateBack.observe2(this) {
            if (it) {
                navigateBack()
            }
        }
    }

    private fun navigateBack() {
        (activity as? MainActivity)?.goBack()
    }
}
