package de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTraceLocationOnboardingBinding
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInFragment
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class CheckInOnboardingFragment : Fragment(R.layout.fragment_trace_location_onboarding), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: CheckInOnboardingViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentTraceLocationOnboardingBinding by viewBinding()
    private val args by navArgs<CheckInOnboardingFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                binding.root.updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.spacing_fab_padding))
            }
        }

        viewModel.isOnboardingComplete.observe2(this) {
            if (it == TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0 && args.uri != null) {
                findNavController().navigate(
                    CheckInOnboardingFragmentDirections.actionCheckInOnboardingFragmentToCheckInsFragment(
                        args.uri,
                        args.cleanHistory
                    )
                )
            }
        }

        viewModel.events.observe2(this) { navEvent ->
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
