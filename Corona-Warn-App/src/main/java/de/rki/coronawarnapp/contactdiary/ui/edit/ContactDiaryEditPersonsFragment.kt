package de.rki.coronawarnapp.contactdiary.ui.edit

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsViewModel.NavigationEvent.ShowPersonDetailSheet
import de.rki.coronawarnapp.databinding.ContactDiaryEditPersonsFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryEditPersonsFragment : Fragment(R.layout.contact_diary_edit_persons_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryEditPersonsViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryEditPersonsFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        viewModel.personsLiveData.observe2(this) {
            setupRecyclerView(it)
        }

        viewModel.navigationEvent.observe2(this) {

            when(it) {
                ShowDeletionConfirmationDialog ->  DialogHelper.showDialog(deleteAll)
                is ShowPersonDetailSheet -> {
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
        R.string.contact_diary_delete_persons_title,
        R.string.contact_diary_delete_persons_message,
        R.string.contact_diary_delete_button_positive,
        R.string.contact_diary_delete_button_negative,
        positiveButtonFunction = {
            viewModel.onDeleteAllConfirmedClick()
        }
    )

    private fun setupRecyclerView(locations: List<ContactDiaryPerson>) {
        // TODO
    }

}
