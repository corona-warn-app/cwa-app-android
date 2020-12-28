package de.rki.coronawarnapp.tracing.ui.details

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsFragmentLayoutBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
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
    private val binding: TracingDetailsFragmentLayoutBinding by viewBindingLazy()

    private val detailsAdapter = TracingDetailsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            adapter = detailsAdapter
        }

        vm.detailsItems.observe2(this) {
            detailsAdapter.update(it)
        }

        vm.buttonStates.observe2(this) {
            binding.tracingDetailsState = it
        }

        binding.riskDetailsHeaderButtonBack.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.riskDetailsButtonUpdate.setOnClickListener {
            vm.updateRiskDetails()
        }
        binding.riskDetailsButtonEnableTracing.setOnClickListener {
            doNavigate(
                TracingDetailsFragmentDirections.actionRiskDetailsFragmentToSettingsTracingFragment()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshData()
        binding.riskDetailsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
