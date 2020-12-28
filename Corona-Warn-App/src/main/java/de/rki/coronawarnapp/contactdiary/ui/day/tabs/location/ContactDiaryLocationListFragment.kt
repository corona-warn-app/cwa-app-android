package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.MarginRecyclerViewDecoration
import de.rki.coronawarnapp.databinding.ContactDiaryLocationListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryLocationListFragment : Fragment(R.layout.contact_diary_location_list_fragment), AutoInject {
    private val binding: ContactDiaryLocationListFragmentBinding by viewBindingLazy()

    private val navArgs by navArgs<ContactDiaryLocationListFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryLocationListViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryLocationListViewModel.Factory
            factory.create(navArgs.selectedDay)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locationListAdapter = ContactDiaryLocationListAdapter {
            viewModel.onLocationSelectionChanged(it)
        }

        binding.contactDiaryLocationListRecyclerView.apply {
            adapter = locationListAdapter
            addItemDecoration(
                MarginRecyclerViewDecoration(
                    resources.getDimensionPixelSize(R.dimen.spacing_tiny)
                )
            )
        }

        viewModel.uiList.observe2(this) {
            locationListAdapter.update(it)
            binding.contactDiaryLocationListNoItemsGroup.isGone = it.isNotEmpty()
        }
    }
}
