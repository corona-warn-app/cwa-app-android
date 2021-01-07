package de.rki.coronawarnapp.contactdiary.ui.edit

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsViewModel.NavigationEvent.ShowPersonDetailSheet
import de.rki.coronawarnapp.contactdiary.ui.edit.adapter.PersonEditAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryEditPersonsFragmentBinding
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

class ContactDiaryEditPersonsFragment : Fragment(R.layout.contact_diary_edit_persons_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryEditPersonsViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryEditPersonsFragmentBinding by viewBindingLazy()

    private lateinit var listAdapter: PersonEditAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }

        viewModel.isListVisible.observe2(this) {
            binding.contactDiaryPersonListNoItemsGroup.isGone = it
        }

        viewModel.isButtonEnabled.observe2(this) {
            binding.deleteButton.isEnabled = it
        }

        viewModel.personsLiveData.observe2(this) {
            listAdapter.update(it, true)
        }

        viewModel.navigationEvent.observe2(this) {

            when (it) {
                ShowDeletionConfirmationDialog -> DialogHelper.showDialog(deleteAllPersonsConfirmationDialog)
                is ShowPersonDetailSheet -> {
                    doNavigate(
                        ContactDiaryEditPersonsFragmentDirections
                            .actionContactDiaryEditPersonsFragmentToContactDiaryPersonBottomSheetDialogFragment(
                                it.person
                            )
                    )
                }
            }
        }

        binding.apply {
            deleteButton.setOnClickListener { viewModel.onDeleteAllPersonsClick() }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setupRecyclerView() {
        listAdapter = PersonEditAdapter(
            clickLabelString = getString(R.string.accessibility_edit),
            getContentDescriptionString = { getString(R.string.accessibility_person, it.fullName) },
            onItemClicked = { viewModel.onEditPersonClick(it) }
        )
        binding.personsRecyclerView.adapter = listAdapter
    }

    private val deleteAllPersonsConfirmationDialog by lazy {
        DialogHelper.DialogInstance(
            requireActivity(),
            R.string.contact_diary_delete_persons_title,
            R.string.contact_diary_delete_persons_message,
            R.string.contact_diary_delete_button_positive,
            R.string.contact_diary_delete_button_negative,
            positiveButtonFunction = {
                viewModel.onDeleteAllConfirmedClick()
            }
        )
    }
}
