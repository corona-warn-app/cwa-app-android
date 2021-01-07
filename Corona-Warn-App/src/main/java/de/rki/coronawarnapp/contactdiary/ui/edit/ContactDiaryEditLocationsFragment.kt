package de.rki.coronawarnapp.contactdiary.ui.edit

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsViewModel.NavigationEvent.ShowLocationDetailSheet
import de.rki.coronawarnapp.contactdiary.ui.edit.adapter.LocationEditAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryEditLocationsFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryEditLocationsFragment : Fragment(R.layout.contact_diary_edit_locations_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryEditLocationsViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryEditLocationsFragmentBinding by viewBindingLazy()

    private lateinit var listAdapter: LocationEditAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }

        binding.deleteButton.setOnClickListener { viewModel.onDeleteAllLocationsClick() }

        viewModel.isListVisible.observe2(this) {
            binding.contactDiaryLocationListNoItemsGroup.isGone = it
        }

        viewModel.isButtonEnabled.observe2(this) {
            binding.deleteButton.isEnabled = it
        }

        viewModel.locationsLiveData.observe2(this) {
            listAdapter.update(it, true)
        }

        viewModel.navigationEvent.observe2(this) {

            when (it) {
                ShowDeletionConfirmationDialog -> DialogHelper.showDialog(deleteAllLocationsConfirmationDialog)
                is ShowLocationDetailSheet -> {
                    doNavigate(
                        ContactDiaryEditLocationsFragmentDirections
                            .actionContactDiaryEditLocationsFragmentToContactDiaryLocationBottomSheetDialogFragment(
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

    private val deleteAllLocationsConfirmationDialog by lazy {
        DialogHelper.DialogInstance(
            requireActivity(),
            R.string.contact_diary_delete_locations_title,
            R.string.contact_diary_delete_locations_message,
            R.string.contact_diary_delete_button_positive,
            R.string.contact_diary_delete_button_negative,
            positiveButtonFunction = {
                viewModel.onDeleteAllConfirmedClick()
            }
        )
    }
}
