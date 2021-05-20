package de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTraceLocationOnboardingBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
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

        if (viewModel.isOnboardingComplete && args.uri != null) {
            doNavigate(
                CheckInOnboardingFragmentDirections.actionCheckInOnboardingFragmentToCheckInsFragment(
                    args.uri,
                    args.cleanHistory
                )
            )
        }

        with(binding) {
            checkInOnboardingAcknowledge.setOnClickListener { viewModel.onAcknowledged() }
            // TODO if consent is already given: should the text be changed?
            if (viewModel.isOnboardingComplete) checkInOnboardingAcknowledge.visibility = View.GONE
            checkInOnboardingPrivacy.setOnClickListener { viewModel.onPrivacy() }

            if (!args.showBottomNav) {
                checkInOnboardingToolbar.apply {
                    navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                    navigationContentDescription = getString(R.string.accessibility_close)
                    setNavigationOnClickListener { popBackStack() }
                }
            }
        }

        viewModel.events.observe2(this) { navEvent ->
            doNavigate(
                when (navEvent) {
                    CheckInOnboardingNavigation.AcknowledgedNavigation ->
                        CheckInOnboardingFragmentDirections.actionCheckInOnboardingFragmentToCheckInsFragment(args.uri)
                    CheckInOnboardingNavigation.DataProtectionNavigation ->
                        CheckInOnboardingFragmentDirections.actionCheckInOnboardingFragmentToPrivacyFragment()
                }
            )
        }
    }
}
