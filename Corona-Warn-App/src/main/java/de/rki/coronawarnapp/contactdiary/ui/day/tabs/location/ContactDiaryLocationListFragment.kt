package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragmentDirections
import de.rki.coronawarnapp.contactdiary.ui.durationpicker.ContactDiaryDurationPickerFragment
import de.rki.coronawarnapp.contactdiary.util.MarginRecyclerViewDecoration
import de.rki.coronawarnapp.databinding.ContactDiaryLocationListFragmentBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.Duration
import javax.inject.Inject

class ContactDiaryLocationListFragment :
    Fragment(R.layout.contact_diary_location_list_fragment),
    AutoInject,
    ContactDiaryDurationPickerFragment.OnChangeListener {

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

        viewModel.openDialog.observe2(this) {
            val args = Bundle()
            args.putString(ContactDiaryDurationPickerFragment.DURATION_ARGUMENT_KEY, it)

            val durationPicker = ContactDiaryDurationPickerFragment()
            durationPicker.arguments = args
            durationPicker.setTargetFragment(this@ContactDiaryLocationListFragment, 0)
            durationPicker.show(parentFragmentManager, "ContactDiaryDurationPickerFragment")
        }

        viewModel.openCommentInfo.observe2(this) {
            doNavigate(
                ContactDiaryDayFragmentDirections
                    .actionContactDiaryDayFragmentToContactDiaryCommentInfoFragment()
            )
        }
    }

    override fun onChange(duration: Duration) {
        viewModel.onDurationSelected(duration)
    }
}
