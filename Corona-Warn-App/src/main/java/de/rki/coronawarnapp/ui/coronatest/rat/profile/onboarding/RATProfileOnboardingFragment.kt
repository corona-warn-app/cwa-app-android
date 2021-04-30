package de.rki.coronawarnapp.ui.coronatest.rat.profile.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.RatProfileOnboardingFragmentBinding
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class RATProfileOnboardingFragment : Fragment(R.layout.rat_profile_onboarding_fragment) {

    private val binding: RatProfileOnboardingFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            nextButton.setOnClickListener {
                doNavigate(
                    RATProfileOnboardingFragmentDirections
                        .actionRatProfileOnboardingFragmentToRatProfileCreateFragment()
                )
            }
        }
}
