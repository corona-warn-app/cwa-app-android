package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ui.observe2

class OnboardingLoadingFragment : Fragment(R.layout.onboaring_loading_layout) {

    private val viewModel: OnboardingLoadingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigationEvents.observe2(this) { event ->
            when (event) {
                OnboardingFragmentEvents.ShowInteropDeltaOnboarding ->
                    findNavController().navigate(
                        OnboardingLoadingFragmentDirections
                            .actionLoadingFragmentToOnboardingDeltaInteroperabilityFragment()
                    )

                OnboardingFragmentEvents.ShowNewReleaseFragment ->
                    findNavController().navigate(
                        OnboardingLoadingFragmentDirections
                            .actionLoadingFragmentToNewReleaseInfoFragment()
                    )

                OnboardingFragmentEvents.ShowOnboarding ->
                    findNavController().navigate(
                        OnboardingLoadingFragmentDirections
                            .actionLoadingFragmentToOnboardingFragment()
                    )

                OnboardingFragmentEvents.OnboardingDone -> {
                    MainActivity.start(requireContext(), requireActivity().intent)
                    activity?.overridePendingTransition(0, 0)
                    activity?.finish()
                }
            }
        }

        viewModel.navigate()
    }
}
