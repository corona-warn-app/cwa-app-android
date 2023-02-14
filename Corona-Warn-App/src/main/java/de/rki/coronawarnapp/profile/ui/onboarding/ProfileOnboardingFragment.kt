package de.rki.coronawarnapp.profile.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ProfileOnboardingFragmentBinding
import de.rki.coronawarnapp.util.ui.addTitleId
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class ProfileOnboardingFragment : Fragment(R.layout.profile_onboarding_fragment) {

    private val args by navArgs<ProfileOnboardingFragmentArgs>()
    private val binding: ProfileOnboardingFragmentBinding by viewBinding()
    private val viewModel: ProfileOnboardingFragmentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.apply {
                setNavigationOnClickListener { popBackStack() }
                addTitleId(R.id.profile_onboarding_fragment_title)
            }
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
