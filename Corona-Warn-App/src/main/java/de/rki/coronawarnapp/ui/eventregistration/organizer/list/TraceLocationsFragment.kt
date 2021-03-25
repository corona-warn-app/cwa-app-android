package de.rki.coronawarnapp.ui.eventregistration.organizer.list

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.android.material.transition.Hold
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.registerOnPageChangeCallback
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsListFragmentBinding
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class TraceLocationsFragment : Fragment(R.layout.trace_location_organizer_trace_locations_list_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: TraceLocationsViewModel by cwaViewModels { viewModelFactory }
    private val binding: TraceLocationOrganizerTraceLocationsListFragmentBinding by viewBindingLazy()
    private val traceLocationsAdapter = TraceLocationsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu(binding.toolbar)

        binding.recyclerView.apply {
            adapter = traceLocationsAdapter
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            onScroll {
                onScrollChange(it)
            }
        }

        binding.toolbar.setOnClickListener {
            popBackStack()
        }

        viewModel.traceLocations.observe2(this) {
            traceLocationsAdapter.update(it)
            binding.apply {
                recyclerView.isGone = it.isEmpty()
                qrCodesListNoItemsGroup.isGone = it.isNotEmpty()
            }
        }

        viewModel.showDeleteSingleDialog.observe2(this) {
            showDeleteSingleDialog(it)
        }

        viewModel.createNewTraceLocationEvent.observe2(this) {
            doNavigate(
                TraceLocationsFragmentDirections
                    .actionTraceLocationOrganizerTraceLocationsListFragmentToTraceLocationOrganizerCategoriesFragment()
            )
        }

        binding.qrCodeFab.setOnClickListener {
            viewModel.createNewTraceLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        inflateMenu(R.menu.menu_trace_location_organizer_list)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_information -> {
                    Toast.makeText(requireContext(), "Information // TODO", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_remove_all -> {
                    showDeleteAllDialog()
                    true
                }
                else -> onOptionsItemSelected(it)
            }
        }
    }

    private fun showDeleteAllDialog() {
        val deleteAllDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.trace_location_organiser_list_delete_all_popup_title,
            R.string.trace_location_organiser_list_delete_all_popup_message,
            R.string.trace_location_organiser_list_delete_all_popup_positive_button,
            R.string.trace_location_organiser_list_delete_all_popup_negative_button,
            positiveButtonFunction = {
                viewModel.deleteAllTraceLocations()
            }
        )
        DialogHelper.showDialog(deleteAllDialog)
    }

    private fun showDeleteSingleDialog(traceLocation: TraceLocation) {
        val deleteAllDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.trace_location_organiser_list_delete_single_popup_title,
            R.string.trace_location_organiser_list_delete_single_popup_message,
            R.string.trace_location_organiser_list_delete_all_popup_positive_button,
            R.string.trace_location_organiser_list_delete_all_popup_negative_button,
            positiveButtonFunction = {
                viewModel.deleteSingleTraceLocation(traceLocation)
            }
        )
        DialogHelper.showDialog(deleteAllDialog)
    }

    fun onScrollChange(extend: Boolean) =
        with(binding.qrCodeFab) {
            if (extend) extend() else shrink()
        }
}
