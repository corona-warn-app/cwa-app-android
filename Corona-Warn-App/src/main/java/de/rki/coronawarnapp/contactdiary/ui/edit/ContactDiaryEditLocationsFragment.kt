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
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditLocationsViewModel.NavigationEvent.ShowLocationDetailSheet
import de.rki.coronawarnapp.contactdiary.util.setClickLabel
import de.rki.coronawarnapp.databinding.ContactDiaryEditLocationsFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
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

    private lateinit var listAdapter: ListAdapter

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
        listAdapter = ListAdapter(getString(R.string.accessibility_edit)) {
            getString(R.string.accessibility_location, it.locationName)
        }
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

    inner class ListAdapter(
        private val clickLabelString: String,
        private val getContentDescriptionString: (ContactDiaryLocation) -> String
    ) : RecyclerView.Adapter<ListAdapter.ViewHolder>(),
        AsyncDiffUtilAdapter<ContactDiaryLocation> {

        override val asyncDiffer: AsyncDiffer<ContactDiaryLocation> = AsyncDiffer(this)

        inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
            val nameTextView = itemView.findViewById<TextView>(R.id.name)
            val itemContainerView = itemView.findViewById<View>(R.id.item_container)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_diary_edit_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ListAdapter.ViewHolder, position: Int) {
            val location = data[position]
            with(viewHolder) {
                nameTextView.text = location.locationName
                itemContainerView.setOnClickListener {
                    viewModel.onEditLocationClick(location)
                }
                itemContainerView.contentDescription = getContentDescriptionString(location)
                itemContainerView.setClickLabel(clickLabelString)
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getItemId(position: Int): Long {
            return data[position].locationId
        }
    }
}
