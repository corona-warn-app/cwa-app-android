package de.rki.coronawarnapp.contactdiary.ui.day.location

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryLocationListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.setInvisible
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

        val locationListAdapter = ContactDiaryLocationListAdapter()
        binding.contactDiaryLocationListRecyclerView.apply {
            adapter = locationListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.locations.observe2(this) {
            locationListAdapter.update(it)

            binding.contactDiaryLocationListNoItemsGroup.setInvisible(it.isNotEmpty())
        }
    }
}
