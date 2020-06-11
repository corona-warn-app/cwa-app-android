package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentOnboardingBinding
import de.rki.coronawarnapp.ui.doNavigate

/**
 * Onboarding starting point.
 */
class OnboardingFragment : Fragment() {
    companion object {
        private val TAG: String? = OnboardingFragment::class.simpleName
    }

    private var _binding: FragmentOnboardingBinding? = null
    private val binding: FragmentOnboardingBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingButtonNext.setOnClickListener {
            findNavController().doNavigate(
                OnboardingFragmentDirections.actionOnboardingFragmentToOnboardingPrivacyFragment()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
