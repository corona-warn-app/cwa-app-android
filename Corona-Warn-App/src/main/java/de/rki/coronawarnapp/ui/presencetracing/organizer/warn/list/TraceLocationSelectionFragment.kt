package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsWarnListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class TraceLocationSelectionFragment :
    Fragment(R.layout.trace_location_organizer_trace_locations_warn_list_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: TraceLocationSelectionViewModel by cwaViewModels { viewModelFactory }
    private val binding: TraceLocationOrganizerTraceLocationsWarnListFragmentBinding by viewBinding()
    private val traceLocationsAdapter = TraceLocationSelectionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            recyclerView.apply {
                adapter = traceLocationsAdapter
            }

            toolbar.setNavigationOnClickListener {
                popBackStack()
            }

            nextButton.setOnClickListener {
                viewModel.goNext()
            }

            qrScanButton.setOnClickListener {
                viewModel.scanQrCode()
            }
        }

        viewModel.state.observe2(this) {
            traceLocationsAdapter.update(it.traceLocations)
            binding.apply {
                recyclerView.isGone = it.traceLocations.isEmpty()
                listNoItemsGroup.isGone = it.traceLocations.isNotEmpty()
            }
            binding.nextButton.isEnabled = it.actionEnabled
        }

        viewModel.events.observe2(this) {
            when (it) {
                is TraceLocationSelectionEvent.ContinueWithTraceLocation -> {
                    findNavController().navigate(
                        TraceLocationSelectionFragmentDirections
                            .actionTraceLocationSelectionFragmentToTraceLocationWarnDurationFragment(
                                traceLocation = it.traceLocation
                            )
                    )
                }
                TraceLocationSelectionEvent.ScanQrCode -> {
                    findNavController().navigate(
                        TraceLocationSelectionFragmentDirections
                            .actionTraceLocationSelectionFragmentToTraceLocationQrScannerFragment()
                    )
                }
            }
        }
    }
}
