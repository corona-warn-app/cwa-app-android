package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.MarginRecyclerViewDecoration
import de.rki.coronawarnapp.databinding.ContactDiaryPersonListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryPersonListFragment : Fragment(R.layout.contact_diary_person_list_fragment), AutoInject {
    private val binding: ContactDiaryPersonListFragmentBinding by viewBindingLazy()

    private val navArgs by navArgs<ContactDiaryPersonListFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryPersonListViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryPersonListViewModel.Factory
            factory.create(navArgs.selectedDay)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val personListAdapter = ContactDiaryPersonListAdapter() {
            viewModel.onPersonSelectionChanged(it)
        }

        binding.contactDiaryPersonListRecyclerView.apply {
            adapter = personListAdapter
            addItemDecoration(
                MarginRecyclerViewDecoration(
                    resources.getDimensionPixelSize(R.dimen.spacing_tiny)
                )
            )
        }

        viewModel.uiList.observe2(this) {
            personListAdapter.update(it)
            binding.contactDiaryPersonListNoItemsGroup.isGone = it.isNotEmpty()
        }
    }
}
