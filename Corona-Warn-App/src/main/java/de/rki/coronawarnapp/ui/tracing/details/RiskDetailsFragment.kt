package de.rki.coronawarnapp.ui.tracing.details

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentRiskDetailsBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This is the detail view of the risk card if additional information for the user.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 */
class RiskDetailsFragment : Fragment(R.layout.fragment_risk_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: RiskDetailsFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentRiskDetailsBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.tracingDetailsState.observe2(this) {
            binding.tracingDetails = it
        }
        vm.tracingCardState.observe2(this) {
            binding.tracingCard = it
        }

        binding.riskDetailsHeaderButtonBack.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.riskDetailsButtonUpdate.setOnClickListener {
            vm.updateRiskDetails()
        }
        binding.riskDetailsButtonEnableTracing.setOnClickListener {
            findNavController().doNavigate(
                RiskDetailsFragmentDirections.actionRiskDetailsFragmentToSettingsTracingFragment()
            )
        }

        binding.riskDetailsInformationLowriskBodyUrl
            .convertToHyperlink(getString(R.string.risk_details_explanation_faq_link))

        binding.riskDetailsInformationLowriskBodyUrl
            .movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onResume() {
        super.onResume()
        vm.refreshData()
        binding.riskDetailsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
