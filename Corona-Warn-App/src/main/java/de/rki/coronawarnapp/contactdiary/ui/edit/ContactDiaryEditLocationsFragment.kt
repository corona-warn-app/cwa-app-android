package de.rki.coronawarnapp.contactdiary.ui.edit

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsViewModel.NavigationEvent.ShowLocationDetailSheet
import de.rki.coronawarnapp.databinding.ContactDiaryEditLocationsFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryEditLocationsFragment : Fragment(R.layout.contact_diary_edit_locations_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryEditLocationsViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryEditLocationsFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        viewModel.locationsLiveData.observe2(this) {
            setupRecyclerView(it)
        }

        viewModel.navigationEvent.observe2(this) {

            when(it) {
                ShowDeletionConfirmationDialog ->  DialogHelper.showDialog(deleteAll)
                is ShowLocationDetailSheet -> {
                    // TODO
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private val deleteAll = DialogHelper.DialogInstance(
        requireActivity(),
        R.string.contact_diary_delete_locations_title,
        R.string.contact_diary_delete_locations_message,
        R.string.contact_diary_delete_button_positive,
        R.string.contact_diary_delete_button_negative,
        positiveButtonFunction = {
            viewModel.onDeleteAllConfirmedClick()
        }
    )

    private fun setupRecyclerView(locations: List<ContactDiaryLocation>) {
        // TODO
    }

}
