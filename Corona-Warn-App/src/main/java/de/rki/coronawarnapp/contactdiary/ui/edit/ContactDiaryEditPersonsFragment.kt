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
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsViewModel.NavigationEvent.ShowPersonDetailFragment
import de.rki.coronawarnapp.contactdiary.ui.edit.adapter.PersonEditAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryEditPersonsFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.addNavigationIconButtonId
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class ContactDiaryEditPersonsFragment : Fragment(R.layout.contact_diary_edit_persons_fragment) {

    private val viewModel: ContactDiaryEditPersonsViewModel by viewModels()
    private val binding: ContactDiaryEditPersonsFragmentBinding by viewBinding()

    private lateinit var listAdapter: PersonEditAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }
        binding.toolbar.addNavigationIconButtonId(R.id.contact_diary_edit_persons_fragment_navigation_icon_buttonId)

        viewModel.isListVisible.observe2(this) {
            binding.contactDiaryPersonListNoItemsGroup.isGone = it
        }

        viewModel.isButtonEnabled.observe2(this) {
            binding.deleteButton.isEnabled = it
        }

        viewModel.personsLiveData.observe2(this) {
            listAdapter.update(it, true)
            if (it.isEmpty()) {
                popBackStack()
            }
        }

        viewModel.navigationEvent.observe2(this) {
            when (it) {
                ShowDeletionConfirmationDialog -> deleteAllPersonsConfirmationDialog()
                is ShowPersonDetailFragment -> {
                    findNavController().navigate(
                        ContactDiaryEditPersonsFragmentDirections
                            .actionContactDiaryEditPersonsFragmentToContactDiaryAddPersonFragment(
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

    private fun deleteAllPersonsConfirmationDialog() = displayDialog {
        title(R.string.contact_diary_delete_persons_title)
        message(R.string.contact_diary_delete_persons_message)
        positiveButton(R.string.contact_diary_delete_button_positive) { viewModel.onDeleteAllConfirmedClick() }
        negativeButton(R.string.contact_diary_delete_button_negative)
        setDeleteDialog(true)
    }
}
