package de.rki.coronawarnapp.ui.eventregistration.attendee.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTraceLocationOnboardingBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class CheckInOnboardingFragment : Fragment(R.layout.fragment_trace_location_onboarding), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: CheckInOnboardingViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentTraceLocationOnboardingBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            checkInOnboardingAcknowledge.setOnClickListener { viewModel.onAcknowledged() }
            checkInOnboardingPrivacy.setOnClickListener { viewModel.onPrivacy() }
        }

        viewModel.events.observe2(this) { navEvent ->
            doNavigate(
                when (navEvent) {
                    CheckInOnboardingNavigation.AcknowledgedNavigation ->
                        CheckInOnboardingFragmentDirections.actionCheckInOnboardingFragmentToCheckInsFragment()
                    CheckInOnboardingNavigation.DataProtectionNavigation ->
                        CheckInOnboardingFragmentDirections.actionCheckInOnboardingFragmentToPrivacyFragment()
                }
            )
        }
    }
}
