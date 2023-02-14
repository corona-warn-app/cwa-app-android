package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragment
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragmentDirections
import de.rki.coronawarnapp.contactdiary.util.MarginRecyclerViewDecoration
import de.rki.coronawarnapp.databinding.ContactDiaryPersonListFragmentBinding
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted

class ContactDiaryPersonListFragment : Fragment(R.layout.contact_diary_person_list_fragment), AutoInject {
    private val binding: ContactDiaryPersonListFragmentBinding by viewBinding()

    private val navArgs by navArgs<ContactDiaryPersonListFragmentArgs>()

    private val viewModel: ContactDiaryPersonListViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryPersonListViewModel.Factory
            factory.create(navArgs.selectedDay)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val personListAdapter = ContactDiaryPersonListAdapter()

        binding.contactDiaryPersonListRecyclerView.apply {
            adapter = personListAdapter
            addItemDecoration(
                MarginRecyclerViewDecoration(
                    resources.getDimensionPixelSize(R.dimen.margin_16)
                )
            )
            onScroll {
                (parentFragment as? ContactDiaryDayFragment)?.onScrollChange(it)
            }
        }

        viewModel.uiList.observe(viewLifecycleOwner) {
            personListAdapter.update(it)
            binding.contactDiaryPersonListNoItemsGroup.isGone = it.isNotEmpty()
        }

        viewModel.openCommentInfo.observe(viewLifecycleOwner) {
            findNavController().navigate(
                ContactDiaryDayFragmentDirections
                    .actionContactDiaryDayFragmentToContactDiaryCommentInfoFragment()
            )
        }
    }
}
