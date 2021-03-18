package de.rki.coronawarnapp.test.organiser.ui

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.android.material.transition.Hold
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.MarginRecyclerViewDecoration
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerQrCodesListFragmentBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class TraceLocationsFragment : Fragment(R.layout.trace_location_organizer_qr_codes_list_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel:TraceLocationsViewModel by cwaViewModels { viewModelFactory }
    private val binding: TraceLocationOrganizerQrCodesListFragmentBinding by viewBindingLazy()
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
            addItemDecoration(MarginRecyclerViewDecoration(resources.getDimensionPixelSize(R.dimen.spacing_tiny)))
        }

        viewModel.traceLocations.observe2(this) {
            traceLocationsAdapter.update(it)
            binding.apply {
                recyclerView.isGone = it.isEmpty()
                qrCodesListNoItemsGroup.isGone = it.isNotEmpty()
            }
        }

        binding.qrCodeFab.setOnClickListener { viewModel.generateEvents(1) }

    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        inflateMenu(R.menu.menu_trace_location_host)
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
            R.string.trace_location_organiser_qr_list_delete_all_popup_title,
            R.string.trace_location_organiser_qr_list_delete_all_popup_message,
            R.string.trace_location_organiser_qr_list_delete_all_popup_positive_button,
            R.string.trace_location_organiser_qr_list_delete_all_popup_negative_button,
            positiveButtonFunction = {
                viewModel.deleteAllTraceLocations()
            }
        )
        DialogHelper.showDialog(deleteAllDialog)
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Organiser QR Codes List",
            description = "Check organiser qr codes list",
            targetId = R.id.test_orginser_qr_codes_list_fragment
        )
    }
}
