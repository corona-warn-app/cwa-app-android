package de.rki.coronawarnapp.tracing.ui.details

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsFragmentLayoutBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.mutateDrawable
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This is the detail view of the risk card if additional information for the user.
 */
class TracingDetailsFragment : Fragment(R.layout.tracing_details_fragment_layout), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: TracingDetailsFragmentViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )
    private val binding: TracingDetailsFragmentLayoutBinding by viewBinding()

    private val detailsAdapter = TracingDetailsAdapter(
        onItemClickListener = { vm.onItemClicked(it) },
        onInfoItemClickListener = { vm.onInfoItemClicked(it) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            adapter = detailsAdapter
        }

        vm.detailsItems.observe(viewLifecycleOwner) {
            detailsAdapter.update(it)
        }

        vm.buttonStates.observe(viewLifecycleOwner) {
            with(binding) {
                toolbar.setBackgroundColor(it.getRiskColor(requireContext()))
                toolbar.setTitleTextColor(it.getStableTextColor(requireContext()))
                riskDetailsButton.isGone = it.isRiskLevelButtonGroupVisible()
                riskDetailsButtonEnableTracing.isGone = it.isRiskDetailsEnableTracingButtonVisible()
                riskDetailsButtonUpdate.isGone = it.isRiskDetailsUpdateButtonVisible()
                riskDetailsButtonUpdate.isEnabled = it.isUpdateButtonEnabled()
                riskDetailsButtonUpdate.text = it.getUpdateButtonText(requireContext())
                toolbar.navigationIcon = closeIcon(it)
            }
        }

        vm.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {
                is TracingDetailsNavigationEvents.NavigateToSurveyConsentFragment -> findNavController().navigate(
                    TracingDetailsFragmentDirections.actionRiskDetailsFragmentToSurveyConsentFragment(it.type)
                )

                is TracingDetailsNavigationEvents.NavigateToSurveyUrlInBrowser -> openUrl(it.url)
                TracingDetailsNavigationEvents.NavigateToHomeRules -> findNavController().navigate(
                    TracingDetailsFragmentDirections.actionRiskDetailsFragmentToHomeRules()
                )

                TracingDetailsNavigationEvents.NavigateToHygieneRules -> findNavController().navigate(
                    TracingDetailsFragmentDirections.actionRiskDetailsFragmentToHygieneRules()
                )
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }
        binding.riskDetailsButtonUpdate.setOnClickListener {
            vm.updateRiskDetails()
        }
        binding.riskDetailsButtonEnableTracing.setOnClickListener {
            findNavController().navigate(
                TracingDetailsFragmentDirections.actionRiskDetailsFragmentToSettingsTracingFragment()
            )
        }
    }

    private fun closeIcon(it: TracingDetailsState) =
        resources.mutateDrawable(
            R.drawable.ic_close,
            it.getStableTextColor(requireContext())
        )

    override fun onResume() {
        super.onResume()
        binding.riskDetailsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
