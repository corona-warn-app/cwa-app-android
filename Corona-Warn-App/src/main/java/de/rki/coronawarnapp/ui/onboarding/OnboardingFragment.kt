package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.databinding.FragmentOnboardingBinding
import de.rki.coronawarnapp.ui.BaseFragment

/**
 * Onboarding starting point.
 */
class OnboardingFragment : BaseFragment() {
    companion object {
        private val TAG: String? = OnboardingFragment::class.simpleName
    }

    private lateinit var binding: FragmentOnboardingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnboardingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingButtonNext.setOnClickListener {
            doNavigate(
                OnboardingFragmentDirections.actionOnboardingFragmentToOnboardingPrivacyFragment()
            )
        }
    }
}
