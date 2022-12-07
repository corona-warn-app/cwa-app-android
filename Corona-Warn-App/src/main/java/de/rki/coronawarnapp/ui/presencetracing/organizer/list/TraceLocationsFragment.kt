package de.rki.coronawarnapp.ui.presencetracing.organizer.list

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsListFragmentBinding
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.traceLocationCategories
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragmentArgs
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.addMenuId
import de.rki.coronawarnapp.util.ui.addTitleId
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class TraceLocationsFragment : Fragment(R.layout.trace_location_organizer_trace_locations_list_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: TraceLocationsViewModel by cwaViewModels { viewModelFactory }
    private val binding: TraceLocationOrganizerTraceLocationsListFragmentBinding by viewBinding()
    private val traceLocationsAdapter = TraceLocationsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
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
            setupSwipe(context = requireContext())
        }

        binding.toolbar.apply {
            setNavigationOnClickListener {
                popBackStack()
            }
            addMenuId(R.id.trace_locations_fragment_menu_id)
            addTitleId(R.id.trace_location_organizer_trace_locations_list_fragment_title_id)
        }

        viewModel.traceLocations.observe2(this) {
            traceLocationsAdapter.update(it)
            binding.apply {
                recyclerView.isGone = it.isEmpty()
                qrCodesListNoItemsGroup.isGone = it.isNotEmpty()
                binding.toolbar.menu.findItem(R.id.menu_remove_all)?.isEnabled = it.isNotEmpty()
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
                    setupHoldTransition()
                    val navigatorExtras = binding.recyclerView.findViewHolderForAdapterPosition(it.position)?.itemView
                        ?.run {
                            FragmentNavigatorExtras(this to this.transitionName)
                        }

                    findNavController().navigate(
                        R.id.action_traceLocationsFragment_to_qrCodeDetailFragment,
                        QrCodeDetailFragmentArgs(traceLocationId = it.id).toBundle(),
                        null,
                        navigatorExtras
                    )
                }

                is TraceLocationEvent.DuplicateItem -> {
                    setupAxisTransition()
                    openCreateEventFragment(it.traceLocation)
                }

                is TraceLocationEvent.StartQrCodePosterFragment -> {
                    setupAxisTransition()
                    findNavController().navigate(

                        TraceLocationsFragmentDirections.actionTraceLocationsFragmentToQrCodePosterFragment(
                            it.traceLocation.id
                        )
                    )
                }

                is TraceLocationEvent.SelfCheckIn -> {
                    findNavController().navigate(
                        CheckInsFragment.createDeepLink(it.traceLocation.locationUrl, true, it.isOnboarded),
                        NavOptions.Builder()
                            .setPopUpTo(R.id.checkInsFragment, true)
                            .build()
                    )
                }
            }
        }

        binding.qrCodeFab.apply {
            setOnClickListener {
                setupHoldTransition()
                findNavController().navigate(
                    R.id.action_traceLocationsFragment_to_traceLocationCategoryFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(this to transitionName)
                )
            }
        }
    }

    private fun setupHoldTransition() {
        exitTransition = Hold()
        reenterTransition = Hold()
    }

    private fun setupAxisTransition() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
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
                    setupAxisTransition()
                    findNavController().navigate(
                        R.id.action_traceLocationOrganizerListFragment_to_traceLocationInfoFragment
                    )
                    true
                }

                R.id.menu_warn_guests -> {
                    setupAxisTransition()
                    findNavController().navigate(
                        R.id.action_traceLocationsFragment_to_traceLocationWarnInfoFragment
                    )
                    true
                }

                R.id.menu_remove_all -> {
                    showDeleteAllDialog()
                    true
                }

                else -> false
            }
        }
    }

    private fun showDeleteAllDialog() = displayDialog {
        title(R.string.trace_location_organiser_list_delete_all_popup_title)
        message(R.string.trace_location_organiser_list_delete_all_popup_message)
        positiveButton(R.string.trace_location_organiser_list_delete_all_popup_positive_button) {
            viewModel.deleteAllTraceLocations()
        }
        negativeButton(R.string.trace_location_organiser_list_delete_all_popup_negative_button)
        setDeleteDialog(true)
    }

    private fun openCreateEventFragment(traceLocation: TraceLocation) {
        val category = traceLocationCategories.find { it.type == traceLocation.type }
        if (category == null) {
            Timber.e("Category not found, traceLocation = $traceLocation")
        } else {
            findNavController().navigate(
                TraceLocationsFragmentDirections.actionTraceLocationsFragmentToTraceLocationCreateFragment(
                    category,
                    traceLocation
                )
            )
        }
    }

    private fun showDeleteSingleDialog(traceLocation: TraceLocation, position: Int?) = displayDialog {
        title(R.string.trace_location_organiser_list_delete_single_popup_title)
        message(R.string.trace_location_organiser_list_delete_single_popup_message)
        positiveButton(R.string.trace_location_organiser_list_delete_all_popup_positive_button) {
            viewModel.deleteSingleTraceLocation(traceLocation)
        }
        negativeButton(R.string.trace_location_organiser_list_delete_all_popup_negative_button) {
            position?.let { traceLocationsAdapter.notifyItemChanged(it) }
        }
        dismissAction { position?.let { traceLocationsAdapter.notifyItemChanged(it) } }
        setDeleteDialog(true)
    }

    private fun onScrollChange(extend: Boolean) =
        with(binding.qrCodeFab) {
            if (extend) extend() else shrink()
        }
}
