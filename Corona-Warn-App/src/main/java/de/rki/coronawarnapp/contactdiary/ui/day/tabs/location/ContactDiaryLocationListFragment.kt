package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

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
import de.rki.coronawarnapp.databinding.ContactDiaryLocationListFragmentBinding
import de.rki.coronawarnapp.ui.durationpicker.DurationPicker
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryLocationListFragment :
    Fragment(R.layout.contact_diary_location_list_fragment),
    AutoInject {

    private val binding: ContactDiaryLocationListFragmentBinding by viewBinding()

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
            addItemDecoration(
                MarginRecyclerViewDecoration(
                    resources.getDimensionPixelSize(R.dimen.list_item_decoration_card_margins)
                )
            )
            onScroll {
                (parentFragment as? ContactDiaryDayFragment)?.onScrollChange(it)
            }
        }

        viewModel.uiList.observe2(this) {
            locationListAdapter.update(it)
            binding.contactDiaryLocationListNoItemsGroup.isGone = it.isNotEmpty()
        }

        viewModel.openDialog.observe2(this) {
            val durationPicker = DurationPicker.Builder()
                .minutes(step = 10)
                .duration(it)
                .title(getString(R.string.duration_dialog_title))
                .build()
            durationPicker.show(parentFragmentManager, "DurationPicker")
            durationPicker.setDurationChangeListener { duration ->
                viewModel.onDurationSelected(duration)
            }
        }

        viewModel.openCommentInfo.observe2(this) {
            findNavController().navigate(
                ContactDiaryDayFragmentDirections
                    .actionContactDiaryDayFragmentToContactDiaryCommentInfoFragment()
            )
        }
    }
}
