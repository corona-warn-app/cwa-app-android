package de.rki.coronawarnapp.ui.coronatest.rat.profile.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.RatProfileOnboardingFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class RATProfileOnboardingFragment : Fragment(R.layout.rat_profile_onboarding_fragment), AutoInject {

    private val binding: RatProfileOnboardingFragmentBinding by viewBinding()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: RATProfileOnboardingFragmentViewModel by cwaViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            nextButton.setOnClickListener {
                viewModel.onNext()
                doNavigate(
                    RATProfileOnboardingFragmentDirections
                        .actionRatProfileOnboardingFragmentToRatProfileCreateFragment()
                )
            }

            ratProfileOnboardingPrivacy.setOnClickListener {
                doNavigate(
                    RATProfileOnboardingFragmentDirections
                        .actionRatProfileOnboardingFragmentToPrivacyFragment()
                )
            }
        }
}
