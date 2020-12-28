package de.rki.coronawarnapp.contactdiary.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditPersonsViewModel.NavigationEvent.ShowPersonDetailSheet
import de.rki.coronawarnapp.databinding.ContactDiaryEditPersonsFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
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

    private val personList: MutableList<ContactDiaryPerson> = mutableListOf()
    private val listAdapter = ListAdapter()

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
            personList.clear()
            personList.addAll(it)
            listAdapter.notifyDataSetChanged()
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

    inner class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

        inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
            val nameTextView = itemView.findViewById<TextView>(R.id.name)
            val itemContainerView = itemView.findViewById<View>(R.id.item_container)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_diary_edit_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ListAdapter.ViewHolder, position: Int) {
            val person = personList[position]
            viewHolder.nameTextView.text = person.fullName
            viewHolder.itemContainerView.setOnClickListener {
                viewModel.onEditPersonClick(person)
            }
        }

        override fun getItemCount(): Int {
            return personList.size
        }
    }
}
