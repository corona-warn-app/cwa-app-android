package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class OnboardingLoadingFragment : Fragment(R.layout.onboaring_loading_layout), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: OnboardingLoadingViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigationEvents.observe2(this) { event ->
            when (event) {
                OnboardingFragmentEvents.ShowInteropDeltaOnboarding ->
                    doNavigate(
                        OnboardingLoadingFragmentDirections
                            .actionLoadingFragmentToOnboardingDeltaInteroperabilityFragment()
                    )
                OnboardingFragmentEvents.ShowNewReleaseFragment ->
                    doNavigate(
                        OnboardingLoadingFragmentDirections
                            .actionLoadingFragmentToNewReleaseInfoFragment()
                    )
                OnboardingFragmentEvents.ShowOnboarding ->
                    doNavigate(
                        OnboardingLoadingFragmentDirections
                            .actionLoadingFragmentToOnboardingFragment()
                    )
                OnboardingFragmentEvents.OnboardingDone -> {
                    MainActivity.start(requireContext(), OnboardingActivity.getShortcutFromIntent(activity?.intent))
                    activity?.overridePendingTransition(0, 0)
                    activity?.finish()
                }
            }
        }

        viewModel.navigate()
    }
}
