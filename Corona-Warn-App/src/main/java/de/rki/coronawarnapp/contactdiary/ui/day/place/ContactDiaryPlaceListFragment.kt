package de.rki.coronawarnapp.contactdiary.ui.day.place

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryPlaceListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryPlaceListFragment : Fragment(R.layout.contact_diary_place_list_fragment), AutoInject {
    private val binding: ContactDiaryPlaceListFragmentBinding by viewBindingLazy()

    private val navArgs by navArgs<ContactDiaryPlaceListFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryPlaceListViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryPlaceListViewModel.Factory
            factory.create(navArgs.selectedDay)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val placeListAdapter = ContactDiaryPlaceListAdapter()
        binding.contactDiaryPlaceListRecyclerView.apply {
            adapter = placeListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.locations.observe2(this) {
            placeListAdapter.update(it)
        }
    }
}
