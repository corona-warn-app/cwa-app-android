package de.rki.coronawarnapp.contactdiary.ui.edit

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsViewModel.NavigationEvent.ShowLocationDetailFragment
import de.rki.coronawarnapp.contactdiary.ui.edit.adapter.LocationEditAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryEditLocationsFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class ContactDiaryEditLocationsFragment : Fragment(R.layout.contact_diary_edit_locations_fragment) {

    private val viewModel: ContactDiaryEditLocationsViewModel by viewModels()
    private val binding: ContactDiaryEditLocationsFragmentBinding by viewBinding()

    private lateinit var listAdapter: LocationEditAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }

        binding.deleteButton.setOnClickListener { viewModel.onDeleteAllLocationsClick() }

        viewModel.isListVisible.observe(viewLifecycleOwner) {
            binding.contactDiaryLocationListNoItemsGroup.isGone = it
        }

        viewModel.isButtonEnabled.observe(viewLifecycleOwner) {
            binding.deleteButton.isEnabled = it
        }

        viewModel.locationsLiveData.observe(viewLifecycleOwner) {
            listAdapter.update(it, true)
            if (it.isEmpty()) {
                popBackStack()
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) {

            when (it) {
                ShowDeletionConfirmationDialog -> deleteAllLocationsConfirmationDialog()
                is ShowLocationDetailFragment -> {
                    findNavController().navigate(
                        ContactDiaryEditLocationsFragmentDirections
                            .actionContactDiaryEditLocationsFragmentToContactDiaryAddLocationFragment(
                                it.location
                            )
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setupRecyclerView() {
        listAdapter = LocationEditAdapter(
            clickLabelString = getString(R.string.accessibility_edit),
            getContentDescriptionString = { getString(R.string.accessibility_location, it.locationName) },
            onItemClicked = { viewModel.onEditLocationClick(it) }
        )
        binding.locationsRecyclerView.adapter = listAdapter
    }

    private fun deleteAllLocationsConfirmationDialog() = displayDialog {
        title(R.string.contact_diary_delete_locations_title)
        message(R.string.contact_diary_delete_locations_message)
        positiveButton(R.string.contact_diary_delete_button_positive) { viewModel.onDeleteAllConfirmedClick() }
        negativeButton(R.string.contact_diary_delete_button_negative)
        setDeleteDialog(true)
    }
}
