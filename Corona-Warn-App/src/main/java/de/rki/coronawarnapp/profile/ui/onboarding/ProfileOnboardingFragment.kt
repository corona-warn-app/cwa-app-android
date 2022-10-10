package de.rki.coronawarnapp.profile.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ProfileOnboardingFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ProfileOnboardingFragment : Fragment(R.layout.profile_onboarding_fragment), AutoInject {

    private val binding: ProfileOnboardingFragmentBinding by viewBinding()
    private val args by navArgs<ProfileOnboardingFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: ProfileOnboardingFragmentViewModel by cwaViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            nextButton.apply {
                setOnClickListener {
                    viewModel.onNext()
                    findNavController().navigate(
                        ProfileOnboardingFragmentDirections
                            .actionProfileOnboardingFragmentToProfileListFragment()
                    )
                }

                isVisible = args.showButton
            }

            ratProfileOnboardingPrivacy.setOnClickListener {
                findNavController().navigate(
                    ProfileOnboardingFragmentDirections
                        .actionProfileOnboardingFragmentToPrivacyFragment()
                )
            }
        }
}
