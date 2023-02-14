package de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTraceLocationOnboardingBinding
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInFragment
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class CheckInOnboardingFragment : Fragment(R.layout.fragment_trace_location_onboarding) {

    private val viewModel: CheckInOnboardingViewModel by viewModels()
    private val binding: FragmentTraceLocationOnboardingBinding by viewBinding()
    private val args by navArgs<CheckInOnboardingFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.checkOnboarding()

        with(binding) {
            checkInOnboardingAcknowledge.setOnClickListener { viewModel.onAcknowledged() }
            checkInOnboardingPrivacy.setOnClickListener { viewModel.onPrivacy() }

            if (!args.showBottomNav) {
                checkInOnboardingToolbar.apply {
                    navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                    navigationContentDescription = getString(R.string.accessibility_close)
                    setNavigationOnClickListener { popBackStack() }
                }
            } else {
                binding.root.updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.padding_80))
            }
        }

        viewModel.events.observe(viewLifecycleOwner) { navEvent ->
            when (navEvent) {
                CheckInOnboardingNavigation.AcknowledgedNavigation -> {
                    val locationId = args.locationId
                    if (locationId != null) {
                        val navOption = NavOptions.Builder()
                            .setPopUpTo(R.id.checkInOnboardingFragment, true)
                            .build()
                        findNavController().navigate(ConfirmCheckInFragment.uri(locationId), navOption)
                    } else {
                        findNavController().navigate(
                            CheckInOnboardingFragmentDirections.actionCheckInOnboardingFragmentToCheckInsFragment(
                                uri = args.uri,
                                cleanHistory = true
                            )
                        )
                    }
                }

                CheckInOnboardingNavigation.SkipOnboardingInfo -> {
                    if (args.showBottomNav || args.isOrganizerOnboarded) {
                        findNavController().navigate(
                            CheckInOnboardingFragmentDirections.actionCheckInOnboardingFragmentToCheckInsFragment(
                                args.uri,
                                args.cleanHistory
                            )
                        )
                    }
                }

                CheckInOnboardingNavigation.DataProtectionNavigation -> findNavController().navigate(
                    CheckInOnboardingFragmentDirections.actionCheckInOnboardingFragmentToPrivacyFragment()
                )
            }
        }
    }

    companion object {
        fun uri(locationId: String) = "cwa://check-in-onboarding/?showBottomNav=false&locationId=$locationId".toUri()
    }
}
