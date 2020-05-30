package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.databinding.FragmentOnboardingPrivacyBinding
import de.rki.coronawarnapp.ui.BaseFragment

/**
 * This fragment informs the user regarding privacy.
 */
class OnboardingPrivacyFragment : BaseFragment() {
    companion object {
        private val TAG: String? = OnboardingPrivacyFragment::class.simpleName
    }

    private lateinit var binding: FragmentOnboardingPrivacyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnboardingPrivacyBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener {
            doNavigate(
                OnboardingPrivacyFragmentDirections.actionOnboardingPrivacyFragmentToOnboardingTracingFragment()
            )
        }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            (activity as OnboardingActivity).goBack()
        }
    }
}
