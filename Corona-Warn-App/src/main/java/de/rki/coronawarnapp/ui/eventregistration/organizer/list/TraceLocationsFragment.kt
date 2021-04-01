package de.rki.coronawarnapp.ui.eventregistration.organizer.list

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.Hold
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsListFragmentBinding
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.adapter.category.traceLocationCategories
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.list.isSwipeable
import de.rki.coronawarnapp.util.list.onSwipeItem
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
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
            onSwipeItem(context = requireContext()) { position, direction ->
                val traceLocationItem = traceLocationsAdapter.data[position]
                if (traceLocationItem.isSwipeable()) {
                    traceLocationItem.onSwipe(position, direction)
                }
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

        viewModel.events.observe2(this) {
            when (it) {
                is TraceLocationEvent.ConfirmDeleteItem -> {
                    showDeleteSingleDialog(it.traceLocation, null)
                }
                is TraceLocationEvent.ConfirmSwipeItem -> {
                    showDeleteSingleDialog(it.traceLocation, it.position)
                }
                is TraceLocationEvent.StartQrCodeDetailFragment -> {
                    doNavigate(
                        TraceLocationsFragmentDirections.actionTraceLocationOrganizerListFragmentToQrCodeDetailFragment(
                            traceLocationId = it.id,
                        )
                    )
                }
                is TraceLocationEvent.DuplicateItem -> {
                    openCreateEventFragment(it.traceLocation)
                }
            }
        }

        binding.qrCodeFab.apply {
            setOnClickListener {
                findNavController().navigate(
                    R.id.action_traceLocationOrganizerListFragment_to_traceLocationOrganizerCategoriesFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(this to transitionName)
                )
            }
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
                    findNavController().navigate(
                        R.id.action_traceLocationOrganizerListFragment_to_traceLocationOrganizerQRInfoFragment
                    )
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

    private fun openCreateEventFragment(traceLocation: TraceLocation) {
        val category = traceLocationCategories.find { it.type == traceLocation.type }
        if (category == null) {
            Timber.e("Category not found, traceLocation = $traceLocation")
        } else {
            findNavController().navigate(
                TraceLocationsFragmentDirections
                    .actionTraceLocationOrganizerListFragmentToTraceLocationCreateFragment(
                        category,
                        traceLocation
                    )
            )
        }
    }

    private fun showDeleteSingleDialog(traceLocation: TraceLocation, position: Int?) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.trace_location_organiser_list_delete_single_popup_title)
            setMessage(R.string.trace_location_organiser_list_delete_single_popup_message)
            setPositiveButton(R.string.trace_location_organiser_list_delete_all_popup_positive_button) { _, _ ->
                viewModel.deleteSingleTraceLocation(traceLocation)
            }
            setNegativeButton(R.string.trace_location_organiser_list_delete_all_popup_negative_button) { _, _ ->
                position?.let {
                    traceLocationsAdapter.notifyItemChanged(
                        position
                    )
                }
            }
            setOnCancelListener {
                position?.let {
                    traceLocationsAdapter.notifyItemChanged(
                        position
                    )
                }
            }
        }.show()
    }

    private fun onScrollChange(extend: Boolean) =
        with(binding.qrCodeFab) {
            if (extend) extend() else shrink()
        }
}
